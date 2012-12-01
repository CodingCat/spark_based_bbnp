package bpnn.neurons

import java.lang.String

import spark.RDD
import spark.SparkEnv
import spark.SparkContext

import bpnn._
import bpnn.utils._

class BiasNeuronUnit(private val nextLayer:NeuronLayer)
		extends NeuronUnit[String, Float](0, 1) {

	override def init() {
		nextLayer ! RegisterInputUnitMsg(this.toString, null)
	}

	override def run() {
		outputRDD = bpNeuronNetworksSetup.sc.parallelize(
			Array.fill[Float](numTrainingInstance)(1), 1)
		nextLayer ! InputUnitReadyMessage(this.toString, outputRDD) 
	}

	override def toString = "biasUnitFor" + nextLayer
}