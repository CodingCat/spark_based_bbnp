package bpnn.neurons

import spark.SparkContext
import spark.RDD

case class InputUnitReadyMessage(id:Int, inputRDD:RDD[Float])
case class TestMsgClass(id:Int)
case class RegisterInputUnitMsg(srcUnitName:String, dstUnitName:String)
