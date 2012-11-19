package bpnn.neurons

import scala.actors.Actor
import scala.collection.mutable.HashMap

import spark.SparkEnv
import spark.SparkContext
import SparkContext._

import bpnn.utils.LayerConf

abstract class NeuronLayer(confPath:String, nLayer:Actor, sEnv:SparkEnv) extends Actor {
	protected val conf = new LayerConf(confPath)
	protected val nextLayer:Actor = nLayer
	protected var numNeurons:Int = 0;
	protected val sparkEnv:SparkEnv = sEnv

	def init
	def runUnits
}