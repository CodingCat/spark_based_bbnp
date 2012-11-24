package bpnn

import spark.SparkEnv
import spark.SparkContext

import bpnn.neurons._
import bpnn.utils._

object bpNeuronNetworks {
	def main (args:Array[String]) {
		
		bpNeuronNetworksSetup.init
		
		val hiddenUnits:HiddenLayer = new HiddenLayer("hidden-conf.xml", null, 
			SparkEnv.get)
		val inputUnits:InputLayer = new InputLayer("input-conf.xml", hiddenUnits, 
			SparkEnv.get)
		
		hiddenUnits.start()
		hiddenUnits ! "initializeUnits"
		inputUnits.start()
		inputUnits ! "initializeUnits"
		
	}
}