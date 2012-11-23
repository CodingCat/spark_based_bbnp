package bpnn.neurons

import scala.xml._
import scala.collection.mutable.LinkedList
import scala.collection.mutable.HashMap
import scala.actors
import scala.actors.Actor

import spark.RDD
import spark.SparkEnv
import spark.SparkContext

import bpnn._
import bpnn.utils._

class InputLayer(confPath:String, nextL:Actor, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, nextL, sEnv) {
	
	private val units = new HashMap[Int, InputNeuronUnit]()

	class InputNeuronUnit(id:Int, inputSplit:Int, inputPath:String)
		extends NeuronUnit[String, Float](id,inputSplit,inputPath) {
		
		override def run() {
			SparkEnv.set(sparkEnv)
			outputRDD = bpNeuronNetworksSetup.sc.
				textFile(inputPath, numInputSplit).map[Float](_.toFloat).cache()
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

	
	override def runUnits() {
		units.foreach((t2) => (t2._2.run())) 
		//bias term
		SparkEnv.set(sparkEnv)
		val biasRDD = bpNeuronNetworksSetup.sc.parallelize(Array.fill[Float](numTrainingInstance)(1))
		biasRDD.cache()
		nextLayer ! inputUnitReadyMessage(-1, biasRDD) 
	}

	//initialize the input layer
	override def init() {
		//parse XML configuration file to get the number of nodes in each layer
		numNeurons = conf.getInt("InputLayer.unitNum", 1)
		println("start " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) {
			val unitName:String = "unit" + i
			units.put(i, 
				new InputNeuronUnit(i, 
					conf.getInt("InputLayer.numInputSplit." + unitName, 1),
					conf.getString("InputLayer.inputPath." + unitName, null)))
		}
	}
}