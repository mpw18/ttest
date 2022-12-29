/*
 * Copyright 2017-2022 John Snow Labs
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

package com.johnsnowlabs.ml.ai

import com.johnsnowlabs.ml.ai.util.PrepareEmbeddings
import com.johnsnowlabs.ml.tensorflow.sentencepiece.{SentencePieceWrapper, SentencepieceEncoder}
import com.johnsnowlabs.ml.tensorflow.sign.{ModelSignatureConstants, ModelSignatureManager}
import com.johnsnowlabs.ml.tensorflow.{TensorResources, TensorflowWrapper}
import com.johnsnowlabs.nlp.annotators.common._

import scala.collection.JavaConverters._

/** This class is used to calculate ALBERT embeddings for For Sequence Batches of
  * WordpieceTokenizedSentence. Input for this model must be tokenzied with a SentencePieceModel,
  *
  * This Tensorflow model is using the weights provided by https://tfhub.dev/google/albert_base/3
  * * sequence_output: representations of every token in the input sequence with shape
  * [batch_size, max_sequence_length, hidden_size].
  *
  * ALBERT: A LITE BERT FOR SELF-SUPERVISED LEARNING OF LANGUAGE REPRESENTATIONS - Google
  * Research, Toyota Technological Institute at Chicago This these embeddings represent the
  * outputs generated by the Albert model. All offical Albert releases by google in TF-HUB are
  * supported with this Albert Wrapper:
  *
  * TF-HUB Models : albert_base = https://tfhub.dev/google/albert_base/3 | 768-embed-dim,
  * 12-layer, 12-heads, 12M parameters albert_large = https://tfhub.dev/google/albert_large/3 |
  * 1024-embed-dim, 24-layer, 16-heads, 18M parameters albert_xlarge =
  * https://tfhub.dev/google/albert_xlarge/3 | 2048-embed-dim, 24-layer, 32-heads, 60M parameters
  * albert_xxlarge = https://tfhub.dev/google/albert_xxlarge/3 | 4096-embed-dim, 12-layer,
  * 64-heads, 235M parameters
  *
  * This model requires input tokenization with SentencePiece model, which is provided by Spark
  * NLP
  *
  * For additional information see : https://arxiv.org/pdf/1909.11942.pdf
  * https://github.com/google-research/ALBERT https://tfhub.dev/s?q=albert
  *
  * Tips:
  *
  * ALBERT uses repeating layers which results in a small memory footprint, however the
  * computational cost remains similar to a BERT-like architecture with the same number of hidden
  * layers as it has to iterate through the same number of (repeating) layers.
  *
  * @param tensorflowWrapper
  *   Albert Model wrapper with TensorFlowWrapper
  * @param spp
  *   Albert SentencePiece model with SentencePieceWrapper
  * @param batchSize
  *   size of batch
  * @param configProtoBytes
  *   Configuration for TensorFlow session
  */
private[johnsnowlabs] class Albert(
    val tensorflowWrapper: TensorflowWrapper,
    val spp: SentencePieceWrapper,
    batchSize: Int,
    configProtoBytes: Option[Array[Byte]] = None,
    signatures: Option[Map[String, String]] = None)
    extends Serializable {

  val _tfAlbertSignatures: Map[String, String] =
    signatures.getOrElse(ModelSignatureManager.apply())

  // keys representing the input and output tensors of the ALBERT model
  private val SentenceStartTokenId = spp.getSppModel.pieceToId("[CLS]")
  private val SentenceEndTokenId = spp.getSppModel.pieceToId("[SEP]")
  private val SentencePadTokenId = spp.getSppModel.pieceToId("[pad]")
  private val SentencePieceDelimiterId = spp.getSppModel.pieceToId("▁")

  def tag(batch: Seq[Array[Int]]): Seq[Array[Array[Float]]] = {

    val maxSentenceLength = batch.map(pieceIds => pieceIds.length).max
    val batchLength = batch.length

    val tensors = new TensorResources()

    val (tokenTensors, maskTensors, segmentTensors) =
      PrepareEmbeddings.prepareBatchTensorsWithSegment(
        tensors = tensors,
        batch = batch,
        maxSentenceLength = maxSentenceLength,
        batchLength = batchLength)

    val runner = tensorflowWrapper
      .getTFSessionWithSignature(
        configProtoBytes = configProtoBytes,
        savedSignatures = signatures,
        initAllTables = false)
      .runner

    runner
      .feed(
        _tfAlbertSignatures.getOrElse(
          ModelSignatureConstants.InputIdsV1.key,
          "missing_input_id_key"),
        tokenTensors)
      .feed(
        _tfAlbertSignatures
          .getOrElse(ModelSignatureConstants.AttentionMaskV1.key, "missing_input_mask_key"),
        maskTensors)
      .feed(
        _tfAlbertSignatures
          .getOrElse(ModelSignatureConstants.TokenTypeIdsV1.key, "missing_segment_ids_key"),
        segmentTensors)
      .fetch(_tfAlbertSignatures
        .getOrElse(ModelSignatureConstants.LastHiddenStateV1.key, "missing_sequence_output_key"))

    val outs = runner.run().asScala
    val embeddings = TensorResources.extractFloats(outs.head)

    tokenTensors.close()
    maskTensors.close()
    segmentTensors.close()
    tensors.clearSession(outs)
    tensors.clearTensors()

    PrepareEmbeddings.prepareBatchWordEmbeddings(
      batch,
      embeddings,
      maxSentenceLength,
      batchLength)

  }

  def predict(
      tokenizedSentences: Seq[TokenizedSentence],
      batchSize: Int,
      maxSentenceLength: Int,
      caseSensitive: Boolean): Seq[WordpieceEmbeddingsSentence] = {

    val wordPieceTokenizedSentences =
      tokenizeWithAlignment(tokenizedSentences, maxSentenceLength, caseSensitive)
    wordPieceTokenizedSentences.zipWithIndex
      .grouped(batchSize)
      .flatMap { batch =>
        val encoded = PrepareEmbeddings.prepareBatchInputsWithPadding(
          batch,
          maxSentenceLength,
          SentenceStartTokenId,
          SentenceEndTokenId,
          SentencePadTokenId)
        val vectors = tag(encoded)

        /*Combine tokens and calculated embeddings*/
        batch.zip(vectors).map { case (sentence, tokenVectors) =>
          val tokenLength = sentence._1.tokens.length
          /*All wordpiece embeddings*/
          val tokenEmbeddings = tokenVectors.slice(1, tokenLength + 1)
          val originalIndexedTokens = tokenizedSentences(sentence._2)

          val tokensWithEmbeddings =
            sentence._1.tokens.zip(tokenEmbeddings).flatMap { case (token, tokenEmbedding) =>
              val tokenWithEmbeddings = TokenPieceEmbeddings(token, tokenEmbedding)
              val originalTokensWithEmbeddings = originalIndexedTokens.indexedTokens
                .find(p =>
                  p.begin == tokenWithEmbeddings.begin && tokenWithEmbeddings.isWordStart)
                .map { token =>
                  val originalTokenWithEmbedding = TokenPieceEmbeddings(
                    TokenPiece(
                      wordpiece = tokenWithEmbeddings.wordpiece,
                      token = if (caseSensitive) token.token else token.token.toLowerCase(),
                      pieceId = tokenWithEmbeddings.pieceId,
                      isWordStart = tokenWithEmbeddings.isWordStart,
                      begin = token.begin,
                      end = token.end),
                    tokenEmbedding)
                  originalTokenWithEmbedding
                }
              originalTokensWithEmbeddings
            }

          WordpieceEmbeddingsSentence(tokensWithEmbeddings, originalIndexedTokens.sentenceIndex)
        }
      }
      .toSeq
  }

  def tokenizeWithAlignment(
      sentences: Seq[TokenizedSentence],
      maxSeqLength: Int,
      caseSensitive: Boolean): Seq[WordpieceTokenizedSentence] = {
    val encoder =
      new SentencepieceEncoder(spp, caseSensitive, delimiterId = SentencePieceDelimiterId)

    val sentenceTokenPieces = sentences.map { s =>
      val trimmedSentence = s.indexedTokens.take(maxSeqLength - 2)
      val wordpieceTokens =
        trimmedSentence.flatMap(token => encoder.encode(token)).take(maxSeqLength)
      WordpieceTokenizedSentence(wordpieceTokens)
    }
    sentenceTokenPieces
  }

}
