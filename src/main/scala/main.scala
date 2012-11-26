package bpnn

import spark.SparkEnv
import spark.SparkContext

import bpnn.neurons._
import bpnn.utils._

object bpNeuronNetworks {
	def main (args:Array[String]) {
		
		bpNeuronNetworksSetup.init
		
		val inputUnits:InputLayer = new InputLayer("input-conf.xml", "InputLayer",
			SparkEnv.get)
		val hiddenUnits:HiddenLayer = new HiddenLayer("hidden-conf.xml", "HiddenLayer",
			SparkEnv.get)
		
		inputUnits.setNextLayer(hiddenUnits)
		hiddenUnits.setPrevLayer(inputUnits)
		
		hiddenUnits.start
		hiddenUnits ! "init"
		/*inputUnits.start
		inputUnits ! "init"*/
		//inputUnits ! "run"
	}
}