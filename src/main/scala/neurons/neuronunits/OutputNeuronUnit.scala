package bpnn.neurons.neuronunits

import scala.collection.mutable.HashMap

import java.lang.String

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.neurons._
import bpnn.utils._

@SerialVersionUID(20L)
class OutputNeuronUnit(
		id:Int, 
		inputSplit:Int,
		inputPath:String)
		extends NeuronUnit[Float, Float](id, inputSplit, inputPath) 
		with Serializable {
	private var labelRDD:RDD[Float] = null

	override def init() {
		super.init()
		labelRDD = bpNeuronNetworksSetup.sc.
			textFile(inputPath, numInputSplit).map[Float](_.toFloat).cache()
	}

	override def run():Boolean = {
		//get the final result
		println("final result run")
		RDDFloatCalculator.setRDDList(inputRDDList)
		outputRDD = RDDFloatCalculator.sigmoid()
		outputRDD.saveAsTextFile("result.txt")
		true
	}

	def transformInputRDD(key:String, readyRDD:RDD[Float]) {
		inputRDDList.put(key, readyRDD.map(inputEle => inputEle * 
						inputWeights.get(key).get))
	}
}
