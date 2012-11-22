package bpnn.neurons

import spark.SparkContext
import spark.RDD

case class inputUnitReadyMessage(id:Int, inputRDD:RDD[Float])
case class testMsgClass(id:Int)
