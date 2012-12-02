package bpnn.utils

import scala.collection.mutable.HashMap

import spark.SparkEnv
import spark.Dependency
import spark.OneToOneDependency
import spark.RDD
import spark.SparkContext
import spark.Split

import bpnn._

object RDDFloatCalculator extends Serializable{

  private var zippedRDD:ZippedRDD[Float] = null
  private var mergedRDD:RDD[Float] = null
  private var RDDList:HashMap[String, RDD[Float]] = null

  def setRDDList(h:HashMap[String, RDD[Float]]) {
    RDDList = h;
  }
    

  def sigmoid():RDD[Float] = {
    val a:Array[RDD[Float]] = new Array[RDD[Float]](RDDList.size)
    RDDList.values.copyToArray(a)
    //merge the input RDDs one by one with zippedRDD
    for (i <- 0 to a.length - 2) {
      println("here")
      if (mergedRDD == null) {
        zippedRDD = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
          a(i), a(i + 1))
      }
      else {
        zippedRDD = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
          mergedRDD, a(i + 1))
      }
      mergedRDD = zippedRDD.map(t2 => (t2._1 + t2._2))
    }
    mergedRDD.map[Float](t => (1/(1 + math.exp(t.toDouble))).toFloat)
  }    
}