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
  private var RDDList:Array[RDD[Float]] = null

  def setRDDList(a:Array[RDD[Float]]) {
    RDDList = a;
  }

  def additiveMerge():RDD[Float] = {
    //merge the input RDDs one by one with zippedRDD
    var mergedRDD:RDD[Float] = null
    for (i <- 0 to RDDList.length - 2) {
      if (mergedRDD == null) {
        zippedRDD = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
          RDDList(i), RDDList(i + 1))
      }
      else {
        zippedRDD = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
          mergedRDD, RDDList(i + 1))
      }
      mergedRDD = zippedRDD.map(t2 => t2._1 + t2._2)
    }
    mergedRDD
  }
  
  def rddSigmoid():RDD[Float] = {
    additiveMerge().map[Float](t => (1/(1 + math.exp(t.toDouble))).toFloat)
  }    
}