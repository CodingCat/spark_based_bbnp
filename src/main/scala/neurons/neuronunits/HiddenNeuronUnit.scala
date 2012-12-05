package bpnn.neurons.neuronunits

import scala.collection.mutable.HashMap

import java.lang.String

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.neurons._
import bpnn.utils._

class HiddenNeuronUnit(
		id:Int, 
		inputSplit:Int,
		inputUnits:Int)
		extends NeuronUnit[Float, Float](id, inputSplit, inputUnits) 
		with Serializable with Logging {
	
	private var labelRDD:RDD[Float] = null
	private var derivativeList:HashMap[String, RDD[Float]] = new HashMap[String, RDD[Float]]
	private var readyRDDList:HashMap[String, RDD[Float]] = new HashMap[String, RDD[Float]]
	private var outDerives:HashMap[String, RDD[Float]] = new HashMap[String, RDD[Float]]
	private var cntReceiveOut:Int = 0	

	override def run():Boolean = {
		//implement the activation function here
		//take into all input dataset into consideration
		//Description of the algorithm here:
		RDDFloatCalculator.setRDDList(inputRDDList.values.toArray)
		outputRDD = RDDFloatCalculator.rddSigmoid()
		resetReadyFlags()
		true
	}

	def receiveDerive(unitInNextLayer:String, derive:RDD[Float]) {
		outDerives.put(unitInNextLayer, derive)
		cntReceiveOut = cntReceiveOut + 1
	}

	def updateWeights() {
		val oneMinusOutput = outputRDD.map(t => 1 - t);
		val productOneOut = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
			oneMinusOutput, outputRDD).map(t2 => t2._1 * t2._2)
		//get the sum of all derives
		RDDFloatCalculator.setRDDList(outDerives.values.toArray)
		val sumOfDerives = RDDFloatCalculator.additiveMerge()
		val prodNoInput = new ZippedRDD[Float](bpNeuronNetworksSetup.sc,
			productOneOut, sumOfDerives).map(t2 => t2._1 * t2._2)

		derivativeList.foreach(t2 =>
			{
				val proNoInputAndInput = new ZippedRDD[Float](bpNeuronNetworksSetup.sc, 
					prodNoInput, readyRDDList.get(t2._1).get)
				val der = proNoInputAndInput.map(t => t._1 * t._2)
				derivativeList.put(t2._1, der)
			}
		)
		//update weights
		//derivativeList.foreach(t2 => t2._2.saveAsTextFile(t2._1))
		inputWeights.foreach(
			t2 => {
				var w = t2._2
				logInfo("before update:" + w)
				val weightsUpdates = derivativeList.get(t2._1).get
				w = w + weightsUpdates.reduce(_ + _)
				logInfo("after update:" + w)
				inputWeights.put(t2._1, w)
			}
		)
		cntReceiveOut = 0	
	}

	def CntReceiveDerive:Int = cntReceiveOut

	def transformInputRDD(key:String, readyRDD:RDD[Float]) {
		readyRDDList.put(key, readyRDD)
		derivativeList.put(key, readyRDD)
		inputRDDList.put(key, readyRDD.map(inputEle => inputEle * 
						inputWeights.get(key).get))
	}

}