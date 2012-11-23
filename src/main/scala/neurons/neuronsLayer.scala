package bpnn.neurons

import scala.actors.Actor

import spark.SparkEnv
import spark.SparkContext

import bpnn.utils.LayerConf

abstract class NeuronLayer(confPath:String, nLayer:Actor, sEnv:SparkEnv) extends Actor {
	protected val conf = new LayerConf(confPath)
	protected val nextLayer:Actor = nLayer
	protected val sparkEnv:SparkEnv = sEnv
	protected var numNeurons:Int = 0
	protected var numTrainingInstance:Int = 0
	
	def init
	def runUnits
}