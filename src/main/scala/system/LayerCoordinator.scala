package bpnn.system

import scala.actors.Actor
import scala.collection.mutable.HashMap

import bpnn.neurons._

//for synchronization
object LayerCoordinator extends Actor {
	val layerList = new HashMap[String, NeuronLayer]()
	val layerReadyFlags = new HashMap[String, Int]()
	var inputLayer:InputLayer = null

	def addLayer(layer:NeuronLayer){
		layerList.put(layer.toString, layer)
		layerReadyFlags.put(layer.toString, 0)
	}

	def setInputLayer(inputL:InputLayer) {
		inputLayer = inputL
	}

	private def run() {
		inputLayer ! "run"
	}

	private def allLayersReady():Boolean = {
		layerReadyFlags.values.foreach(t => if (t == 0) return false)
		true
	}

	def act() {
		loop {
			react {
				case LayerReadyMsg(readylayer) => {
					layerReadyFlags.put(readylayer.toString, 1)
					if (allLayersReady) run()
				}
			}
		}
	}
}