package bpnn.neurons.neuronunits

import scala.collection.mutable.HashMap

import java.lang.String

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.neurons._
import bpnn.utils._

@SerialVersionUID(30L)
class InputNeuronUnit(
		id:Int, 
		inputSplit:Int,
		inputPath:String)
		extends NeuronUnit[String, Float](id, inputSplit, 0, inputPath) 
		with Serializable{

	override def run():Boolean = {
		outputRDD = bpNeuronNetworksSetup.sc.
			textFile(inputPath, numInputSplit).map[Float](_.toFloat).cache()
		true
	}

}