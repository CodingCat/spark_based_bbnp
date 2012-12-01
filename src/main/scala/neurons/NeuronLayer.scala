package bpnn.neurons

import scala.actors.Actor
import scala.collection.mutable.HashMap

import spark.SparkEnv
import spark.SparkContext

import bpnn.utils.LayerConf

abstract class NeuronLayer(
	confPath:String, 
	protected val layerName:String,
	protected val sparkEnv:SparkEnv) extends Actor {
	
	protected var prevLayer:NeuronLayer = null
	protected var nextLayer:NeuronLayer = null
	protected val conf = new LayerConf(confPath)
	protected var numNeurons:Int = 0
	protected var biasUnit:BiasNeuronUnit = null

	def init
	def runUnits {}

	def setPrevLayer(pL:NeuronLayer) {
		prevLayer = pL
	}

	def setNextLayer(nL:NeuronLayer) {
		nextLayer = nL
	}

	override def toString = layerName
}