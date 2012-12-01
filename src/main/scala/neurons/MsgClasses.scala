package bpnn.neurons

import spark.SparkContext
import spark.RDD

case class InputUnitReadyMessage(inputUnitName:String, inputRDD:RDD[Float])
case class TestMsgClass(id:Int)
case class RegisterInputUnitMsg(srcUnitName:String, dstUnitName:String)
case class PrevLayerReadyMsg(readyLayer:NeuronLayer)
case class LayerReadyMsg(Layer:NeuronLayer)