package bpnn.neurons.neuronunits

import scala.collection.mutable.HashMap

import java.lang.String
import java.lang.Math

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.neurons._
import bpnn.utils._

class OutputNeuronUnit(
		id:Int, 
		inputSplits:Int,
		inputUnits:Int, 
		inputPath:String)
		extends NeuronUnit[Float, Float](id, inputSplits, inputUnits, inputPath) 
		with Serializable with Logging {
			
	private var labelRDD:RDD[Float] = null
	private var derivativeList:HashMap[String, RDD[Float]] = new HashMap[String, RDD[Float]]
	private var readyRDDList:HashMap[String, RDD[Float]] = new HashMap[String, RDD[Float]]

	
	override def init() {
		super.init()
		labelRDD = bpNeuronNetworksSetup.sc.
			textFile(inputPath, 1).map[Float](_.toFloat).cache()
	}

	override def run():Boolean = {
		//get the final result
		RDDFloatCalculator.setRDDList(inputRDDList.values.toArray)
		outputRDD = RDDFloatCalculator.rddSigmoid()
		//calculate derivatives
		var error = 0.0f;
		val outAndLabel = new ZippedRDD[Float](bpNeuronNetworksSetup.sc,
			outputRDD, labelRDD)
		val predictError = outAndLabel.map(t2 => t2._1 - t2._2)
		val oneMinusOutput = outputRDD.map(t => 1 - t);
		val productOutError = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
			predictError, outputRDD).map(t2 => t2._1 * t2._2)
		val productNoInput = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
			productOutError, oneMinusOutput).map(t2 => t2._1 * t2._2)
		derivativeList.foreach(t2 =>
			{
				val proNoInputAndInput = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
					productNoInput, readyRDDList.get(t2._1).get)
				derivativeList.put(t2._1, proNoInputAndInput.map(t => t._1 * t._2))
			}
		)
		//update weights
		//derivativeList.foreach(t2 => t2._2.saveAsTextFile(t2._1))
		inputWeights.foreach(
			t2 => {
				val w = t2._2
				val weightsUpdates = derivativeList.get(t2._1).get
				weightsUpdates.map(update => w + update)
				inputWeights.put(t2._1, w)
				println(inputWeights.get(t2._1).get)
			}
		)
		val r = predictError.reduce((a, b) => 
			0.5f * (Math.pow(a.toDouble, 2.0) + Math.pow(b.toDouble, 2.0)).toFloat)
		//compute error 
		//predictError.saveAsTextFile("error")
		logInfo("Prediction Error:" + r)
		true
	}

	def transformInputRDD(key:String, readyRDD:RDD[Float]) {
		readyRDDList.put(key, readyRDD)
		derivativeList.put(key, readyRDD)
		inputRDDList.put(key, readyRDD.map(inputEle => inputEle * 
						inputWeights.get(key).get))
	}
}
