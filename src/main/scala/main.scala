package bpnn

import bpnn.neurons._

object bpNeuronNetworks {
	def main (args:Array[String]) {
		val hiddenUnits:HiddenLayer = new HiddenLayer("hidden-conf.xml", null)
		val inputUnits:InputLayer = new InputLayer("input-conf.xml", hiddenUnits)
		inputUnits.start()
		hiddenUnits.start()
		inputUnits ! "initializeUnits"
		inputUnits ! "start"
	}
}