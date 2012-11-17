package bpnn.neurons

import spark.RDD
import spark.SparkContext
import SparkContext._

import bpnn.utils.LayerConf
		
abstract class NeuronUnit() {
	var neuronId:Int = 0
	var numInputSplit:Int = 1
	var inputRDD:RDD[String] = null
	var outputRDD:RDD[String] = null
	var inputRDDName:String = null
	var outputRDDName:String = null
	var sc:SparkContext = null
	val globalConf:LayerConf = new LayerConf("global-conf.xml")
	def init()
	def run()
}
