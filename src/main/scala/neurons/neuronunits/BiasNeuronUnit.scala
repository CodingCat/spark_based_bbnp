package bpnn.neurons.neuronunits

import java.lang.String

import spark.RDD
import spark.SparkEnv
import spark.SparkContext

import bpnn._
import bpnn.utils._
import bpnn.neurons._

@SerialVersionUID(50L)
class BiasNeuronUnit(
	private val nextLayer:NeuronLayer)
		extends NeuronUnit[String, Float](0, 1) with Serializable{

	override def init() {
		LayerConf.loadConfiguration("global-conf.xml")
		nextLayer ! RegisterInputUnitMsg(this.toString)
	}

	override def run():Boolean = {
		outputRDD = bpNeuronNetworksSetup.sc.parallelize(
			Array.fill[Float](LayerConf.getInt("global.TrainingSet.Num", 100))(1), 1)
		true
	}

	override def toString = "biasUnitFor" + nextLayer
}