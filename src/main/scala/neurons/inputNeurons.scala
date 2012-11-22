package bpnn.neurons

import scala.xml._
import scala.collection.mutable.LinkedList
import scala.collection.mutable.HashMap
import scala.actors
import scala.actors.Actor
import scala.util.Random

import spark.RDD
import spark.SparkEnv
import spark.SparkContext
import SparkContext._

import bpnn._
import bpnn.utils._

class InputLayer(confPath:String, nextL:Actor, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, nextL, sEnv) {
	
	val units = HashMap[Int, InputNeuronUnit]()
	

	class InputNeuronUnit(id:Int)
		extends NeuronUnit {
		
		var inputPath:String = ""
		neuronId = id
		
		override def init(){
			val neuronIdStr:String = neuronId.toString()
			inputPath = conf.getString(
				(new StringBuilder("InputLayer.inputPath.unit" + neuronIdStr)).toString(),
				 "all")
			numInputSplit = conf.getInt(
				(new StringBuilder("InputLayer.numInputSplit.unit" + neuronIdStr)).toString(),
				1)
		}

		override def run() {
			SparkEnv.set(sparkEnv)
			val outputRDD:RDD[Float] = bpNeuronNetworksSetup.sc.
				textFile(inputPath, numInputSplit).map[Float](_.toFloat).cache()
			nextLayer ! testMsgClass(neuronId)
			nextLayer ! inputUnitReadyMessage(neuronId, outputRDD) 

		}
	}

	def act() {
		loop {
			react {
				case "initializeUnits" =>
					init()
				case "start" =>
					println("start run")
					runUnits()
			}
		}
	}

	def initUnits() = { units.foreach((t2) => (t2._2.init())) }
	
	override def runUnits() = { units.foreach((t2) => (t2._2.run())) }

	// initialize the input layer
	override def init() {
		//parse XML configuration file to get the number of nodes in each layer
		numNeurons = conf.getInt("InputLayer.unitNum", 1)
		println("start " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) 
			units.put(i, new InputNeuronUnit(i))
		initUnits()
	}
}