package bpnn

import spark.SparkEnv
import spark.SparkContext

import bpnn.neurons._
import bpnn.utils._
import bpnn.system.LayerCoordinator

object bpNeuronNetworks {
	def main (args:Array[String]) {
		
		bpNeuronNetworksSetup.init
		

		val inputUnits:InputLayer = new InputLayer("input-conf.xml", "InputLayer",
			SparkEnv.get)
		val hiddenUnits:HiddenLayer = new HiddenLayer("hidden-conf.xml", "HiddenLayer",
			SparkEnv.get)
		val outputUnits:OutputLayer = new OutputLayer("output-conf.xml", "OutputLayer", 
			SparkEnv.get)
		
		LayerCoordinator.addLayer(inputUnits)
		LayerCoordinator.addLayer(hiddenUnits)
		LayerCoordinator.addLayer(outputUnits)
		LayerCoordinator.setInputLayer(inputUnits)
		LayerCoordinator.start

		inputUnits.setNextLayer(hiddenUnits)
		hiddenUnits.setPrevLayer(inputUnits)
		hiddenUnits.setNextLayer(outputUnits)
		outputUnits.setPrevLayer(hiddenUnits)
		
		outputUnits.start
		outputUnits ! "init"
	}
}