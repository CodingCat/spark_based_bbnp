package bpnn.system

import scala.actors.Actor
import scala.collection.mutable.HashMap

import bpnn._
import bpnn.utils.Logging
import bpnn.neurons._

//for synchronization
object LayerCoordinator extends Actor with Logging{
	private val layerList = new HashMap[String, NeuronLayer]()
	private val layerReadyFlags = new HashMap[String, Int]()
	private val roundFinishFlags = new HashMap[String, Int]()
	private var inputLayer:InputLayer = null
	private var currentRound = 0;

	def addLayer(layer:NeuronLayer){
		layerList.put(layer.toString, layer)
		layerReadyFlags.put(layer.toString, 0)
		roundFinishFlags.put(layer.toString, 0)
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

	private def CurrentRoundFinish():Boolean = {
		roundFinishFlags.foreach(t2 => {
				println(t2._1 + ":" + t2._2)
				if (t2._1.substring(0, 5) != "Input" && t2._2 == 0) return false
			}
		)
		currentRound = currentRound + 1;
		true
	}

	def act() {
		loop {
			react {
				case LayerReadyMsg(readylayer) => {
					layerReadyFlags.put(readylayer.toString, 1)
					if (allLayersReady) run()
				}
				case RoundFinishMsg(readyLayer) => {
					println("receive roundfinish msg from " + readyLayer.toString)
					roundFinishFlags.put(readyLayer, 1)
					if (CurrentRoundFinish == true) {
						//reset everything 
						if (currentRound >= bpNeuronNetworksSetup.numIterations) {
							layerReadyFlags.foreach(t2 => layerReadyFlags.put(t2._1, 0))
							roundFinishFlags.foreach(t2 => roundFinishFlags.put(t2._1, 0))
						}
						else {
							println("current round:" + currentRound + " total itr num:" + 
								bpNeuronNetworksSetup.numIterations)
							inputLayer ! "run"
						}
					}
				}
			}
		}
	}
}