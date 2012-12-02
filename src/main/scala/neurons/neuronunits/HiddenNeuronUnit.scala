package bpnn.neurons.neuronunits

import scala.collection.mutable.HashMap

import java.lang.String

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.neurons._
import bpnn.utils._

@SerialVersionUID(40L)
class HiddenNeuronUnit(
		id:Int, 
		inputSplit:Int)
		extends NeuronUnit[Float, Float](id, inputSplit) with Serializable {
	
	override def run():Boolean = {
		//implement the activation function here
		//take into all input dataset into consideration
		//Description of the algorithm here:
		RDDFloatCalculator.setRDDList(inputRDDList)
		outputRDD = RDDFloatCalculator.sigmoid()
		println(outputRDD.count)
		resetReadyFlags()
		true
	}



	def transformInputRDD(key:String, readyRDD:RDD[Float]) {
		println(key)
		println(inputWeights.get(key).get)
		if (readyRDD == null) println("bad")
		inputRDDList.put(key, readyRDD.map(inputEle => inputEle * 
						inputWeights.get(key).get))
		//println(readyRDD.count)
	}

}