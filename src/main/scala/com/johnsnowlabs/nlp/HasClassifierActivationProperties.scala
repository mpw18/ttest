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

package com.johnsnowlabs.nlp

import org.apache.spark.ml.param.{BooleanParam, FloatParam, Param}

trait HasClassifierActivationProperties extends ParamsAndFeaturesWritable {

  /** Whether to enable caching DataFrames or RDDs during the training (Default depends on model).
    *
    * @group param
    */
  val activation: Param[String] = new Param(
    this,
    "activation",
    "Whether to calculate logits via Softmax or Sigmoid. Default is Softmax")

  /** Choose the threshold to determine which logits are considered to be positive or negative.
    * (Default: `0.5f`). The value should be between 0.0 and 1.0. Changing the threshold value
    * will affect the resulting labels and can be used to adjust the balance between precision and
    * recall in the classification process.
    *
    * @group param
    */
  val threshold = new FloatParam(
    this,
    "threshold",
    "Choose the threshold to determine which logits are considered to be positive or negative")

  /** Whether or not the result should be multi-class (the sum of all probabilities is 1.0) or
    * multi-label (each label has a probability between 0.0 to 1.0). Default is False i.e.
    * multi-class
    *
    * @group param
    */
  val multilabel: BooleanParam = new BooleanParam(
    this,
    "multilabel",
    "Whether or not the result should be multi-class (the sum of all probabilities is 1.0) or multi-label (each label has a probability between 0.0 to 1.0). Default is False i.e. multi-class")

  /** @group getParam */
  def getActivation: String = {
    val activation =
      if ($(multilabel)) ActivationFunction.sigmoid else ActivationFunction.softmax

    if ($(multilabel)) activation else $(this.activation)
  }

  /** @group setParam */
  def setActivation(value: String): this.type = {

    value match {
      case ActivationFunction.softmax =>
        set(this.activation, ActivationFunction.softmax)
      case ActivationFunction.sigmoid =>
        set(this.activation, ActivationFunction.sigmoid)
      case _ =>
        set(this.activation, ActivationFunction.softmax)
    }

  }

  /** Choose the threshold to determine which logits are considered to be positive or negative.
    * (Default: `0.5f`). The value should be between 0.0 and 1.0. Changing the threshold value
    * will affect the resulting labels and can be used to adjust the balance between precision and
    * recall in the classification process.
    *
    * @group param
    */
  def setThreshold(threshold: Float): this.type =
    set(this.threshold, threshold)

  /** Set whether or not the result should be multi-class (the sum of all probabilities is 1.0) or
    * multi-label (each label has a probability between 0.0 to 1.0). Default is False i.e.
    * multi-class
    *
    * @group param
    */
  def setMultilabel(value: Boolean): this.type = {
    if (value) {
      setActivation(ActivationFunction.sigmoid)
    } else setActivation(ActivationFunction.softmax)
    set(this.multilabel, value)
  }

  setDefault(activation -> ActivationFunction.softmax, threshold -> 0.5f, multilabel -> false)

}

object ActivationFunction {

  val softmax = "softmax"
  val sigmoid = "sigmoid"

}
