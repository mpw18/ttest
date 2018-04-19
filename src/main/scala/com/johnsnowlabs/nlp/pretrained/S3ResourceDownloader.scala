package com.johnsnowlabs.nlp.pretrained

import java.io.File
import java.nio.file.Files
import java.sql.Timestamp
import java.util.Calendar
import java.util.zip.ZipInputStream

import org.apache.hadoop.fs.Path

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSCredentials, AWSStaticCredentialsProvider}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest

import scala.collection.mutable


class S3ResourceDownloader(bucket: String,
                           s3Path: String,
                           cacheFolder: String,
                           credentials: Option[AWSCredentials] = None,
                           region: String = "us-east-1"
                          )
  extends ResourceDownloader with AutoCloseable {

  // repository Folder -> repository Metadata
  val repoFolder2Metadata = mutable.Map[String, RepositoryMetadata]()
  val cachePath = new Path(cacheFolder)

  if (!fs.exists(cachePath)) {
    fs.mkdirs(cachePath)
  }

  lazy val client = {

    val builder = AmazonS3ClientBuilder.standard()
    if (credentials.isDefined)
      builder.setCredentials(new AWSStaticCredentialsProvider(credentials.get))

    builder.setRegion(region)
    val config = new ClientConfiguration()
    config.setSocketTimeout(2000*1000)

    builder.setClientConfiguration(config)

    builder.build()
  }


  private def downloadMetadataIfNeed(folder: String): List[ResourceMetadata] = {
    val lastState = repoFolder2Metadata.get(folder)

    val fiveMinsBefore = getTimestamp(-5)
    val needToRefersh = lastState.isEmpty || lastState.get.lastMetadataDownloaded.before(fiveMinsBefore)

    if (!needToRefersh) {
      lastState.get.metadata
    }
    else {
      val metaFile = getS3File(s3Path, folder, "metadata.json")
      val obj = client.getObject(bucket, metaFile)
      val metadata = ResourceMetadata.readResources(obj.getObjectContent)
      val version = obj.getObjectMetadata.getVersionId

      RepositoryMetadata(metaFile, folder, version, getTimestamp(), metadata)

      metadata
    }
  }

  def resolveLink(request: ResourceRequest): Option[ResourceMetadata] = {
    val metadata = downloadMetadataIfNeed(request.folder)
    ResourceMetadata.resolveResource(metadata, request)
  }

  /**
    * Download resource to local file
    *
    * @param request        Resource request
    * @return               Downloaded file or None if resource is not found
    */
  override def download(request: ResourceRequest): Option[String] = {

    val link = resolveLink(request)
    link.flatMap {
      resource =>
        val s3FilePath = getS3File(s3Path, request.folder, resource.fileName)
        val dstFile = new Path(cachePath.toString, resource.fileName)
        if (!client.doesObjectExist(bucket, s3FilePath)) {
          None
        } else {
          if (!fs.exists(dstFile)) {

            //val obj = client.getObject(bucket, s3FilePath)
            // 1. Create tmp file
            val tmpFileName = Files.createTempFile(resource.fileName, "").toString
            val tmpFile = new File(tmpFileName)

            // 2. Download content to tmp file
            val req = new GetObjectRequest(bucket, s3FilePath)
            client.getObject(req, tmpFile)
            //FileUtils.copyInputStreamToFile(obj.getObjectContent, tmpFile)

            // 3. Move tmp file to destination
            fs.moveFromLocalFile(new Path(tmpFile.toString), dstFile)
          }

          // 4. Unzip if needs
          if (resource.isZipped) {
            val zis = new ZipInputStream(fs.open(dstFile))
            val buf = Array.ofDim[Byte](1024)
            var entry = zis.getNextEntry
            require(dstFile.toString.substring(dstFile.toString.length - 4) == ".zip")
            val splitPath = dstFile.toString.substring(0, dstFile.toString.length - 4)
            while (entry != null) {
              if (!entry.isDirectory) {
                val entryName = new Path(splitPath, entry.getName)
                val outputStream = fs.create(entryName)
                var bytesRead = zis.read(buf, 0, 1024)
                while (bytesRead > -1) {
                  outputStream.write(buf, 0, bytesRead)
                  bytesRead = zis.read(buf, 0, 1024)
                }
                outputStream.close()
              }
              zis.closeEntry()
              entry = zis.getNextEntry
            }
            zis.close()
            Some(splitPath)
          } else {
            Some(dstFile.getName)
          }
        }
    }
  }

  override def close(): Unit = {
    client.shutdown()
  }

  override def clearCache(request: ResourceRequest): Unit = {
    val metadata = downloadMetadataIfNeed(request.folder)

    val resources = ResourceMetadata.resolveResource(metadata, request)
    for (resource <- resources) {
      val fileName = new Path(cachePath.toString, resource.fileName)
      if (fs.exists(fileName))
        fs.delete(fileName, true)

      if (resource.isZipped) {
        require(fileName.toString.substring(fileName.toString.length - 4) == ".zip")
        val unzipped = fileName.toString.substring(0, fileName.toString.length - 4)
        val unzippedFile = new Path(unzipped)
        if (fs.exists(unzippedFile))
          fs.delete(unzippedFile, true)
      }
    }
  }

  private def getTimestamp(addMinutes: Int = 0): Timestamp = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MINUTE, addMinutes)
    val timestamp = new Timestamp(cal.getTime().getTime())
    cal.clear()
    timestamp
  }

  private def getS3File(parts: String*): String = {
    parts
      .map(part => part.stripSuffix("/"))
      .filter(part => part.nonEmpty)
      .mkString("/")
  }
}
