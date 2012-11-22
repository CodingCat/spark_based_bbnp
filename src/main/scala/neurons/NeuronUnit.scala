package bpnn.neurons

import scala.actors.Actor

import spark.RDD
import spark.SparkContext
import SparkContext._

abstract class NeuronUnit() {
	var neuronId:Int = 0
	var numInputSplit:Int = 1
	var inputRDDName:String = null
	
	def init()
	def run()
}
