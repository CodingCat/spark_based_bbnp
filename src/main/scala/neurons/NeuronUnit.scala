package bpnn.neurons

import scala.actors.Actor

import spark.RDD
import spark.SparkContext

import bpnn.utils.LayerConf

abstract class NeuronUnit[InputType, OutputType](
	protected var neuronId:Int,
	protected var numInputSplit:Int,
	private var inputPath:String = null
	) {
		var inputRDD:RDD[InputType] = null
		var outputRDD:RDD[OutputType] = null
		
		def run()
		override def toString = "unit" + neuronId
}
