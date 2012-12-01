package bpnn.utils

import spark.Dependency
import spark.OneToOneDependency
import spark.RDD
import spark.SparkContext
import spark.Split

class RDDCalculator[T](
  private val RDDList:Array[RDD[T]]) {
  
}