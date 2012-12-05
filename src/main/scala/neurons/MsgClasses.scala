package bpnn.neurons

import scala.collection.mutable.HashMap

import spark.SparkContext
import spark.RDD

case class InputUnitReadyMessage(inputUnitName:String, inputRDD:RDD[Float])
case class TestMsgClass(id:Int)
case class RegisterInputUnitMsg(srcUnitName:String)
case class PrevLayerReadyMsg(readyLayer:NeuronLayer)
case class LayerReadyMsg(Layer:NeuronLayer)
case class OutputReadyMsg()
case class StartPrevLayerMsg(NextLayerSize:Int)
case class DeriveListReadyMsg(readyUnit:String, readyRDDMap:HashMap[String, RDD[Float]])
case class RoundFinishMsg(readyLayerName:String)