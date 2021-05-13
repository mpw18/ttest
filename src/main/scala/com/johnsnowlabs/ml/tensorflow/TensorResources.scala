package com.johnsnowlabs.ml.tensorflow

import org.tensorflow.ndarray.buffer._
import org.tensorflow.ndarray.{Shape, StdArrays}
import org.tensorflow.op.Scope
import org.tensorflow.op.core._
import org.tensorflow.op.linalg.MatMul
import org.tensorflow.types._
import org.tensorflow.types.family.{TNumber, TType}
import org.tensorflow.{Operand, Tensor}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.existentials

/**
  * This class is being used to initialize Tensors of different types and shapes for Tensorflow operations
  */
class TensorResources {
  private val tensors = ArrayBuffer[Tensor[_]]()

  def createTensor[T](obj: T): Tensor[_ <: TType] = {
    val result = obj match {
      case float: Float =>
        TFloat32.scalarOf(float)

      case str: String =>
        TString.scalarOf(str)

      case array: Array[String] =>
        TString.tensorOf(StdArrays.ndCopyOf(array))

      case floatArray: Array[Float] =>
        TFloat32.tensorOf(StdArrays.ndCopyOf(floatArray))

      case bidimArray: Array[Array[String]] =>
        TString.tensorOf(StdArrays.ndCopyOf(bidimArray))

      case bidimArray: Array[Array[Float]] =>
        TFloat32.tensorOf(StdArrays.ndCopyOf(bidimArray))

      case tridimArray: Array[Array[Array[Float]]] =>
        TFloat32.tensorOf(StdArrays.ndCopyOf(tridimArray))

      case array: Array[Int] =>
        TInt32.tensorOf(StdArrays.ndCopyOf(array))

      case array: Array[Array[Int]] =>
        TInt32.tensorOf(StdArrays.ndCopyOf(array))

      case array: Array[Array[Array[Int]]] =>
        TInt32.tensorOf(StdArrays.ndCopyOf(array))

      case array: Array[Array[Array[Byte]]] =>
        TUint8.tensorOf(StdArrays.ndCopyOf(array))

    }
    tensors.append(result)
    result
  }


  def createIntBufferTensor(shape: Array[Long], buf: IntDataBuffer): Tensor[TInt32] = {
    val result = TInt32.tensorOf(Shape.of(shape:_*), buf)
    tensors.append(result)
    result
  }

  def createLongBufferTensor(shape: Array[Long], buf: LongDataBuffer): Tensor[TInt64] = {
    val result = TInt64.tensorOf(Shape.of(shape:_*), buf)
    tensors.append(result)
    result
  }

  def createFloatBufferTensor(shape: Array[Long], buf: FloatDataBuffer): Tensor[TFloat32] = {
    val result = TFloat32.tensorOf(Shape.of(shape:_*), buf)
    tensors.append(result)
    result
  }

  def clearTensors(): Unit = {
    for (tensor <- tensors) {
      tensor.close()
    }

    tensors.clear()
  }

  def clearSession(outs: mutable.Buffer[Tensor[_]]): Unit = {
    outs.foreach(_.close())
  }

  def createIntBuffer(dim: Int): IntDataBuffer = {
    DataBuffers.ofInts(dim)
  }

  def createLongBuffer(dim: Int): LongDataBuffer = {
    DataBuffers.ofLongs(dim)
  }

  def createFloatBuffer(dim: Int): FloatDataBuffer = {
    DataBuffers.ofFloats(dim)
  }
}

object TensorResources {
  // TODO all these implementations are not tested

  def calculateTensorSize(source: Tensor[_], size: Option[Int]): Int = {
    size.getOrElse{
      // Calculate real size from tensor shape
      val shape = source.shape()
      shape.asArray.foldLeft(1L)(_*_).toInt
    }
  }

  def extractInts(source: Tensor[_], size: Option[Int] = None): Array[Int] = {
    val realSize = calculateTensorSize(source ,size)
    val buffer = Array.fill(realSize)(0)
    source.rawData.asInts.read(buffer)
    buffer
  }

  def extractInt(source: Tensor[_], size: Option[Int] = None): Int =
    extractInts(source).head

  def extractLongs(source: Tensor[_], size: Option[Int] = None): Array[Long] = {
    val realSize = calculateTensorSize(source ,size)
    val buffer = Array.fill(realSize)(0L)
    source.rawData.asLongs.read(buffer)
    buffer
  }

  def extractFloats(source: Tensor[_], size: Option[Int] = None): Array[Float] = {
    val realSize = calculateTensorSize(source ,size)
    val buffer = Array.fill(realSize)(0f)
    source.rawData.asFloats.read(buffer)
    buffer
  }

  def reverseTensor[T <: TNumber](scope: Scope, tensor: Operand[T], dimension: Int): Operand[T] = {
    val axis = Constant.vectorOf(scope, Array[Int](dimension))
    Reverse.create(scope, tensor, axis)
  }

  def concatTensors[T <: TNumber](scope: Scope, tensors: Seq[Operand[T]], dimension: Int): Operand[T] = {
    val axis: Operand[TInt32] = Constant.vectorOf(scope, Array[Int](dimension))
    val concatTensor = tensors.head.data() match {
      case float: TFloat32 => Concat.create(scope, tensors.asInstanceOf[Seq[Operand[TFloat32]]].toList.asJava, axis)
      case int: TInt32 => Concat.create(scope, tensors.asInstanceOf[Seq[Operand[TInt32]]].toList.asJava, axis)
    }
    concatTensor.asInstanceOf[Operand[T]]
  }

  def reshapeTensor[T <: TNumber](scope: Scope, tensor: Operand[T], shape: Array[Int]): Operand[T] = {
    val reshapedTensor = Reshape.create(scope, tensor, Constant.vectorOf(scope, shape))
    reshapedTensor
  }

  def sliceTensor[T <: TNumber](scope: Scope, tensor: Operand[T], begin: Array[Int], size: Array[Int]): Operand[T] = {
    val slicedTensor = Slice.create(scope, tensor, Constant.vectorOf(scope, begin), Constant.vectorOf(scope, size))
    slicedTensor
  }

  def matmulTensors[T <: TNumber](scope: Scope, tensorA: Operand[T], tensorB: Operand[T]): Operand[T] = {
    val matmul = MatMul.create(scope, tensorA, tensorB)
    matmul
  }

  def stackTensors[T <: TNumber](scope: Scope, tensors: Seq[Operand[T]]): Operand[T] = {
    val stackedTensor = tensors.head.data() match {
      case float: TFloat32 => Stack.create(scope, tensors.asInstanceOf[Seq[Operand[TFloat32]]].toList.asJava)
      case int: TInt32 =>   Stack.create(scope, tensors.asInstanceOf[Seq[Operand[TInt32]]].toList.asJava)
    }
    stackedTensor.asInstanceOf[Operand[T]]
  }

}