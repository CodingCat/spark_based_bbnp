package bpnn.neurons

import scala.actors.Actor

import spark.RDD
import spark.SparkContext
import SparkContext._

abstract class NeuronUnit() {
	var neuronId:Int = 0
	var numInputSplit:Int = 1
	var inputRDD:RDD[String] = null
	var outputRDD:RDD[String] = null
	var inputRDDName:String = null
	var outputRDDName:String = null
	
	def init()
	def run()
}
