/*
 * Copyright 2017-2021 John Snow Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.johnsnowlabs.nlp.embeddings

import com.johnsnowlabs.ml.tensorflow.{TensorflowWrapper, _}
import com.johnsnowlabs.ml.tensorflow.sentencepiece._
import com.johnsnowlabs.ml.tensorflow.wrap.TFWrapper
import com.johnsnowlabs.nlp._
import com.johnsnowlabs.nlp.annotators.common._
import com.johnsnowlabs.nlp.serialization.MapFeature
import com.johnsnowlabs.storage.HasStorageRef
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.param.{IntArrayParam, IntParam}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.io.File

/** XlnetEmbeddings (XLNet): Generalized Autoregressive Pretraining for Language Understanding
 *
 * XLNet is a new unsupervised language representation learning method based on a novel generalized permutation language
 * modeling objective. Additionally, XLNet employs Transformer-XL as the backbone model, exhibiting excellent performance
 * for language tasks involving long context. Overall, XLNet achieves state-of-the-art (SOTA) results on various
 * downstream language tasks including question answering, natural language inference, sentiment analysis, and document
 * ranking.
 *
 * These word embeddings represent the outputs generated by the XLNet models.
 *
 * Note that this is a very computationally expensive module compared to word embedding modules that only perform embedding lookups.
 * The use of an accelerator is recommended.
 *
 * `"xlnet_large_cased"` = [[https://storage.googleapis.com/xlnet/released_models/cased_L-24_H-1024_A-16.zip XLNet-Large]] | 24-layer, 1024-hidden, 16-heads
 *
 * `"xlnet_base_cased"` = [[https://storage.googleapis.com/xlnet/released_models/cased_L-12_H-768_A-12.zip XLNet-Base]]    |  12-layer, 768-hidden, 12-heads. This model is trained on full data (different from the one in the paper).
 *
 * Pretrained models can be loaded with `pretrained` of the companion object:
 * {{{
 * val embeddings = XlnetEmbeddings.pretrained()
 *   .setInputCols("sentence", "token")
 *   .setOutputCol("embeddings")
 * }}}
 * The default model is `"xlnet_base_cased"`, if no name is provided.
 *
 * For extended examples of usage, see the [[https://github.com/JohnSnowLabs/spark-nlp-workshop/blob/master/jupyter/training/english/dl-ner/ner_xlnet.ipynb Spark NLP Workshop]]
 * and the [[https://github.com/JohnSnowLabs/spark-nlp/blob/master/src/test/scala/com/johnsnowlabs/nlp/embeddings/XlnetEmbeddingsTestSpec.scala XlnetEmbeddingsTestSpec]].
 * Models from the HuggingFace 🤗 Transformers library are also compatible with Spark NLP 🚀. The Spark NLP Workshop
 * example shows how to import them [[https://github.com/JohnSnowLabs/spark-nlp/discussions/5669]].
 *
 * '''Sources :'''
 *
 * [[https://arxiv.org/abs/1906.08237 XLNet: Generalized Autoregressive Pretraining for Language Understanding]]
 *
 * [[https://github.com/zihangdai/xlnet]]
 *
 * '''Paper abstract: '''
 *
 * ''With the capability of modeling bidirectional contexts, denoising autoencoding based pretraining like BERT achieves
 * better performance than pretraining approaches based on autoregressive language modeling. However, relying on
 * corrupting the input with masks, BERT neglects dependency between the masked positions and suffers from a pretrain-finetune
 * discrepancy. In light of these pros and cons, we propose XLNet, a generalized autoregressive pretraining method that
 * (1) enables learning bidirectional contexts by maximizing the expected likelihood over all permutations of the
 * factorization order and (2) overcomes the limitations of BERT thanks to its autoregressive formulation. Furthermore,
 * XLNet integrates ideas from Transformer-XL, the state-of-the-art autoregressive model, into pretraining. Empirically,
 * under comparable experiment settings, XLNet outperforms BERT on 20 tasks, often by a large margin, including question
 * answering, natural language inference, sentiment analysis, and document ranking.''
 *
 * ==Example==
 * {{{
 * import spark.implicits._
 * import com.johnsnowlabs.nlp.base.DocumentAssembler
 * import com.johnsnowlabs.nlp.annotators.Tokenizer
 * import com.johnsnowlabs.nlp.embeddings.XlnetEmbeddings
 * import com.johnsnowlabs.nlp.EmbeddingsFinisher
 * import org.apache.spark.ml.Pipeline
 *
 * val documentAssembler = new DocumentAssembler()
 *   .setInputCol("text")
 *   .setOutputCol("document")
 *
 * val tokenizer = new Tokenizer()
 *   .setInputCols("document")
 *   .setOutputCol("token")
 *
 * val embeddings = XlnetEmbeddings.pretrained()
 *   .setInputCols("token", "document")
 *   .setOutputCol("embeddings")
 *
 * val embeddingsFinisher = new EmbeddingsFinisher()
 *   .setInputCols("embeddings")
 *   .setOutputCols("finished_embeddings")
 *   .setOutputAsVector(true)
 *   .setCleanAnnotations(false)
 *
 * val pipeline = new Pipeline().setStages(Array(
 *   documentAssembler,
 *   tokenizer,
 *   embeddings,
 *   embeddingsFinisher
 * ))
 *
 * val data = Seq("This is a sentence.").toDF("text")
 * val result = pipeline.fit(data).transform(data)
 *
 * result.selectExpr("explode(finished_embeddings) as result").show(5, 80)
 * +--------------------------------------------------------------------------------+
 * |                                                                          result|
 * +--------------------------------------------------------------------------------+
 * |[-0.6287205219268799,-0.4865287244319916,-0.186111718416214,0.234187275171279...|
 * |[-1.1967450380325317,0.2746637463569641,0.9481253027915955,0.3431355059146881...|
 * |[-1.0777631998062134,-2.092679977416992,-1.5331977605819702,-1.11190271377563...|
 * |[-0.8349916934967041,-0.45627787709236145,-0.7890847325325012,-1.028069257736...|
 * |[-0.134845569729805,-0.11672890186309814,0.4945235550403595,-0.66587203741073...|
 * +--------------------------------------------------------------------------------+
 * }}}
 *
 * @see [[https://nlp.johnsnowlabs.com/docs/en/annotators Annotators Main Page]] for a list of transformer based embeddings
 * @param uid required internal uid for saving annotator
 * @groupname anno Annotator types
 * @groupdesc anno Required input and expected output annotator types
 * @groupname Ungrouped Members
 * @groupname param Parameters
 * @groupname setParam Parameter setters
 * @groupname getParam Parameter getters
 * @groupname Ungrouped Members
 * @groupprio param  1
 * @groupprio anno  2
 * @groupprio Ungrouped 3
 * @groupprio setParam  4
 * @groupprio getParam  5
 * @groupdesc param A list of (hyper-)parameter keys this annotator can take. Users can set and get the parameter values through setters and getters, respectively.
 */
class XlnetEmbeddings(override val uid: String)
  extends AnnotatorModel[XlnetEmbeddings]
    with HasBatchedAnnotate[XlnetEmbeddings]
    with WriteTensorflowModel
    with WriteSentencePieceModel
    with HasEmbeddingsProperties
    with HasStorageRef
    with HasCaseSensitiveProperties {

  /** Annotator reference id. Used to identify elements in metadata or to refer to this annotator type */
  def this() = this(Identifiable.randomUID("XLNET_EMBEDDINGS"))

  /** Input Annotator Type : TOKEN, DOCUMENT
   *
   * @group anno
   * */
  override val inputAnnotatorTypes: Array[String] = Array(AnnotatorType.DOCUMENT, AnnotatorType.TOKEN)
  /** Output Annotator Type : WORD_EMBEDDINGS
   *
   * @group anno
   * */
  override val outputAnnotatorType: AnnotatorType = AnnotatorType.WORD_EMBEDDINGS

  /** ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()
   *
   * @group param
   * */
  val configProtoBytes = new IntArrayParam(this, "configProtoBytes", "ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()")

  /** @group getSaram */
  def setConfigProtoBytes(bytes: Array[Int]): XlnetEmbeddings.this.type = set(this.configProtoBytes, bytes)

  /** @group setGaram */
  def getConfigProtoBytes: Option[Array[Byte]] = get(this.configProtoBytes).map(_.map(_.toByte))

  /** Max sentence length to process (Default: `128`)
   *
   * @group param
   * */
  val maxSentenceLength = new IntParam(this, "maxSentenceLength", "Max sentence length to process")

  /** @group setParam */
  def setMaxSentenceLength(value: Int): this.type = {
    require(value <= 512, "XLNet model does not support sequences longer than 512 because of trainable positional embeddings")
    require(value >= 1, "The maxSentenceLength must be at least 1")
    set(maxSentenceLength, value)
    this
  }

  /** @group getParam */
  def getMaxSentenceLength: Int = $(maxSentenceLength)

  /**
   * Set dimension of Embeddings
   * Since output shape depends on the model selected, see [[https://github.com/zihangdai/xlnet]]for further reference
   *
   * @group setParam
   * */
  override def setDimension(value: Int): this.type = {
    if (get(dimension).isEmpty)
      set(this.dimension, value)
    this
  }

  /**
   * It contains TF model signatures for the laded saved model
   *
   * @group param
   * */
  val signatures = new MapFeature[String, String](model = this, name = "signatures")

  /** @group setParam */
  def setSignatures(value: Map[String, String]): this.type = {
    if (get(signatures).isEmpty)
      set(signatures, value)
    this
  }

  /** @group getParam */
  def getSignatures: Option[Map[String, String]] = get(this.signatures)

  /** The Tensorflow XLNet Model */
  private var _model: Option[Broadcast[TensorflowXlnet]] = None

  /** Sets XLNet tensorflow Model */
  def setModelIfNotSet(spark: SparkSession, tensorflow: TFWrapper[_], spp: SentencePieceWrapper): this.type = {
    if (_model.isEmpty) {

      _model = Some(
        spark.sparkContext.broadcast(
          new TensorflowXlnet(
            tensorflow,
            spp,
            configProtoBytes = getConfigProtoBytes,
            signatures = getSignatures
          )
        )
      )
    }

    this
  }

  /** Gets XLNet tensorflow Model */
  def getModelIfNotSet: TensorflowXlnet = _model.get.value

  setDefault(
    batchSize -> 8,
    dimension -> 768,
    maxSentenceLength -> 128,
    caseSensitive -> true
  )

  /**
   * takes a document and annotations and produces new annotations of this annotator's annotation type
   *
   * @param batchedAnnotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
   * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
   */
  override def batchAnnotate(batchedAnnotations: Seq[Array[Annotation]]): Seq[Seq[Annotation]] = {
    val batchedTokenizedSentences: Array[Array[TokenizedSentence]] = batchedAnnotations.map(annotations =>
      TokenizedWithSentence.unpack(annotations).toArray
    ).toArray

    /*Return empty if the real tokens are empty*/
    if (batchedTokenizedSentences.nonEmpty) batchedTokenizedSentences.map(tokenizedSentences => {
      val embeddings = getModelIfNotSet.calculateEmbeddings(
        tokenizedSentences,
        $(batchSize),
        $(maxSentenceLength),
        $(caseSensitive)
      )
      WordpieceEmbeddingsSentence.pack(embeddings)
    }) else {
      Seq(Seq.empty[Annotation])
    }
  }

  override def onWrite(path: String, spark: SparkSession): Unit = {
    super.onWrite(path, spark)
    writeTensorflowModelV2(path, spark, getModelIfNotSet.tensorflow, "_xlnet", XlnetEmbeddings.tfFile, configProtoBytes = getConfigProtoBytes)
    writeSentencePieceModel(path, spark, getModelIfNotSet.spp, "_xlnet", XlnetEmbeddings.sppFile)

  }

  override protected def afterAnnotate(dataset: DataFrame): DataFrame = {
    dataset.withColumn(getOutputCol, wrapEmbeddingsMetadata(dataset.col(getOutputCol), $(dimension), Some($(storageRef))))
  }

}

trait ReadablePretrainedXlnetModel extends ParamsAndFeaturesReadable[XlnetEmbeddings] with HasPretrained[XlnetEmbeddings] {
  override val defaultModelName: Some[String] = Some("xlnet_base_cased")

  /** Java compliant-overrides */
  override def pretrained(): XlnetEmbeddings = super.pretrained()

  override def pretrained(name: String): XlnetEmbeddings = super.pretrained(name)

  override def pretrained(name: String, lang: String): XlnetEmbeddings = super.pretrained(name, lang)

  override def pretrained(name: String, lang: String, remoteLoc: String): XlnetEmbeddings = super.pretrained(name, lang, remoteLoc)
}

trait ReadXlnetTensorflowModel extends ReadTensorflowModel with ReadSentencePieceModel {
  this: ParamsAndFeaturesReadable[XlnetEmbeddings] =>

  override val tfFile: String = "xlnet_tensorflow"
  override val sppFile: String = "xlnet_spp"

  def readTensorflow(instance: XlnetEmbeddings, path: String, spark: SparkSession): Unit = {
    val tf = readTensorflowModel(path, spark, "_xlnet_tf", initAllTables = false)
    val spp = readSentencePieceModel(path, spark, "_xlnet_spp", sppFile)
    instance.setModelIfNotSet(spark, tf, spp)
  }

  addReader(readTensorflow)

  def loadSavedModel(tfModelPath: String, spark: SparkSession): XlnetEmbeddings = {

    val f = new File(tfModelPath)
    val sppModelPath = tfModelPath + "/assets"
    val savedModel = new File(tfModelPath, "saved_model.pb")
    val sppModel = new File(sppModelPath, "spiece.model")

    require(f.exists, s"Folder $tfModelPath not found")
    require(f.isDirectory, s"File $tfModelPath is not folder")
    require(
      savedModel.exists(),
      s"savedModel file saved_model.pb not found in folder $tfModelPath"
    )
    require(sppModel.exists(), s"SentencePiece model spiece.model not found in folder $sppModelPath")

    val (wrapper, signatures) = TensorflowWrapper.read(tfModelPath, zipped = false, useBundle = true)
    val spp = SentencePieceWrapper.read(sppModel.toString)

    val _signatures = signatures match {
      case Some(s) => s
      case None => throw new Exception("Cannot load signature definitions from model!")
    }

    new XlnetEmbeddings()
      .setSignatures(_signatures)
      .setModelIfNotSet(spark, wrapper, spp)

  }
}


/**
 * This is the companion object of [[XlnetEmbeddings]]. Please refer to that class for the documentation.
 */
object XlnetEmbeddings extends ReadablePretrainedXlnetModel with ReadXlnetTensorflowModel
