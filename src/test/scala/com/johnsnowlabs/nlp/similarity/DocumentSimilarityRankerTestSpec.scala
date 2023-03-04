package com.johnsnowlabs.nlp.similarity

import com.johnsnowlabs.nlp.AnnotatorType.{DOC_SIMILARITY_RANKINGS, SENTENCE_EMBEDDINGS}
import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.annotators.similarity.{DocumentSimilarityRanker, DocumentSimilarityRankerApproach, DocumentSimilarityRankerModel}
import com.johnsnowlabs.nlp.base.DocumentAssembler
import com.johnsnowlabs.nlp.embeddings.SentenceEmbeddings
import com.johnsnowlabs.nlp.util.io.ResourceHelper
import com.johnsnowlabs.nlp.{AnnotatorBuilder, EmbeddingsFinisher}
import com.johnsnowlabs.tags.SlowTest
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpec

class DocumentSimilarityRankerTestSpec extends AnyFlatSpec {
  val spark: SparkSession = ResourceHelper.spark

  "DocumentSimilarityRanker" should "should rank document similarity" taggedAs SlowTest in {

    val smallCorpus = spark.createDataFrame(
      List(
        "First document, this is my first sentence. This is my second sentence.",
        "Second document, this is my first sentence. This is my second sentence.",
        "Third document, climate change is arguably one of the most pressing problems of our time.",
        "Fourth document, Florence in Italy, is among the most beautiful cities in Europe.",
        "Fifth document, The French Riviera is the Mediterranean coastline of the southeast corner of France.",
      ).map(Tuple1(_)))
      .toDF("text")

    val documentAssembler = new DocumentAssembler()
      .setInputCol("text")
      .setOutputCol("document")

    val sentence = new SentenceDetector()
      .setInputCols("document")
      .setOutputCol("sentence")

    val tokenizer = new Tokenizer()
      .setInputCols(Array("document"))
      .setOutputCol("token")

    val embeddings = AnnotatorBuilder
      .getGLoveEmbeddings(smallCorpus)
      .setInputCols("document", "token")
      .setOutputCol("embeddings")
      .setCaseSensitive(false)

    val embeddingsSentence = new SentenceEmbeddings()
      .setInputCols(Array("document", "embeddings"))
      .setOutputCol("sentence_embeddings")
      .setPoolingStrategy("AVERAGE")

    val sentenceFinisher = new EmbeddingsFinisher()
      .setInputCols("sentence_embeddings")
      .setOutputCols("finished_sentence_embeddings")
      .setCleanAnnotations(false)

    val docSimilarityRanker = new DocumentSimilarityRankerApproach()
      .setInputCols("sentence_embeddings")
      .setOutputCol(DOC_SIMILARITY_RANKINGS)
      .setSimilarityMethod("brp")
      .setNumberOfNeighbours(1)

    // val docSimilarityFinalizer

    val pipeline = new Pipeline()
      .setStages(
        Array(
          documentAssembler,
          sentence,
          tokenizer,
          embeddings,
          embeddingsSentence,
          sentenceFinisher,
          docSimilarityRanker
//          docSimilarityFinalizer
        ))

    val pipelineDF = pipeline.fit(smallCorpus).transform(smallCorpus)

    pipelineDF.printSchema
    pipelineDF.show(false)

  }
}