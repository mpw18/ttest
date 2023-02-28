package com.johnsnowlabs.nlp.annotators.similarity

import com.johnsnowlabs.nlp.AnnotatorType.{DOC_SIMILARITY_RANKINGS, SENTENCE_EMBEDDINGS}
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, HasSimpleAnnotate}
import org.apache.spark.ml.feature.BucketedRandomProjectionLSHModel
import org.apache.spark.ml.param.Param
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}

class DocumentSimilarityRankerModel(override val uid: String)
  extends AnnotatorModel[DocumentSimilarityRankerModel]
    with HasSimpleAnnotate[DocumentSimilarityRankerModel] {

  override val inputAnnotatorTypes: Array[AnnotatorType] = Array(SENTENCE_EMBEDDINGS)

  override val outputAnnotatorType: AnnotatorType = DOC_SIMILARITY_RANKINGS

  def this() = this(Identifiable.randomUID("DOC_SIMILARITY_RANKER"))

  /** The similarity method used to calculate the neighbours.
   * (Default: `"brp"`, Bucketed Random Projection for Euclidean Distance)
   *
   * @group param
   */
  val similarityMethod = new Param[String](
    this,
    "similarityMethod",
    """The similarity method used to calculate the neighbours.
      |(Default: `"brp"`, Bucketed Random Projection for Euclidean Distance)
      |""".stripMargin)
  def setSimilarityMethod(value: String): this.type = set(similarityMethod, value)

  def getSimilarityMethod: String = $(similarityMethod)

  /** The number of neighbours the model will return (Default:`"10"`).
   *
   * @group param
   */
  val numberOfNeighbours = new Param[Int](
    this,
    "numberOfNeighbours",
    """The number of neighbours the model will return (Default:`"10"`)""")

  def setNumberOfNeighbours(value: Int): this.type = set(numberOfNeighbours, value)

  def getNumberOfNeighbours: Int = $(numberOfNeighbours)

  val bucketLength = new Param[Double](
    this,
    "bucketLength",
    """The bucket length that controls the average size of hash buckets.
      |A larger bucket length (i.e., fewer buckets) increases the probability of features being hashed
      |to the same bucket (increasing the numbers of true and false positives)
      |""".stripMargin)

  def setBucketLength(value: Double): this.type = set(bucketLength, value)

  def getBucketLength: Double = $(bucketLength)

  val numHashTables = new Param[Int](
    this,
    "numHashTables",
    """number of hash tables, where increasing number of hash tables lowers the false negative rate,
      |and decreasing it improves the running performance.
      |""".stripMargin)

  def setNumHashTables(value: Int): this.type = set(numHashTables, value)

  def getNumHashTables: Int = $(numHashTables)

  val similarityModel = new Param[BucketedRandomProjectionLSHModel](
    this,
    "similarityModel", "similarityModel LSH based")
  def setLSHModel(value: BucketedRandomProjectionLSHModel): this.type = set(similarityModel, value)

  def getLSHModel: BucketedRandomProjectionLSHModel = $(similarityModel)

  setDefault(
    inputCols -> Array(SENTENCE_EMBEDDINGS),
    outputCol -> DOC_SIMILARITY_RANKINGS,
    similarityMethod -> "brp",
    numberOfNeighbours -> 10,
    bucketLength -> 2.0,
    numHashTables -> 3
  )

  /** takes a document and annotations and produces new annotations of this annotator's annotation
   * type
   *
   * @param annotations
   * Annotations that correspond to inputAnnotationCols generated by previous annotators if any
   * @return
   * any number of annotations processed for every input annotation. Not necessary one to one
   * relationship
   */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    println("into the annotator")
    Seq(Annotation("ciao"))

//    iterate over input annotations
//    - select the input ID based on result input text?
//      - replace features value with embeddings
//      - calculate the N closest IDs with their distances
//      - insert closest and distance in as metadata arrays of N=numNeighbors
//      - forward the embeddings in the metadata embeddings
//    $(similarityModel).transform()
  }

}

object DocumentSimilarityRanker extends DefaultParamsReadable[DocumentSimilarityRankerModel]