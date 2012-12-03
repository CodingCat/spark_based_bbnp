package bpnn.neurons

import scala.xml._
import scala.collection.mutable.LinkedList
import scala.collection.mutable.HashMap
import scala.actors
import scala.actors.Actor

import java.lang.String

import spark.RDD
import spark.SparkEnv
import spark.SparkContext

import bpnn._
import bpnn.system.LayerCoordinator
import bpnn.utils._
import neuronunits._

class InputLayer (
	confPath:String, 
	layerName:String, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, layerName, sEnv) with Logging {
	
	private val units = new HashMap[String, InputNeuronUnit]()

	def act() {
		loop {
			react {
				case "start" =>
					nextLayer ! PrevLayerReadyMsg(this)
				case "init" =>
					init()
					LayerCoordinator ! LayerReadyMsg(this)
				case "run" =>
					println("start run")
					runUnits()
			}
		}
	}

	
	override def runUnits() {
		SparkEnv.set(sparkEnv)
		units.foreach(
			t2 => 
			{
				println(t2._2 + " is running")
				if (t2._2.run() == true) {
					println(t2._2.toString + " is sending InputUnitReadyMessage")
					nextLayer ! InputUnitReadyMessage(t2._2.toString, t2._2.getOutput)
				}
			}
		) 
		if (biasUnit.run == true) {
			nextLayer ! InputUnitReadyMessage(biasUnit.toString, biasUnit.getOutput) 
		}
	}

	//initialize the input layer
	override def init() {
		//parse XML configuration file to get the number of nodes in each layer
		SparkEnv.set(sparkEnv)
		val numInputSplits:Int = conf.getInt("OutputLayer.numInputSplits", 1)
		numNeurons = conf.getInt("InputLayer.unitNum", 1)
		println("InputLayer starts " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) {
			val unitName:String = "unit" + i
			units.put(unitName, 
				new InputNeuronUnit(i, 
					numInputSplits,
					conf.getString("InputLayer.inputPath." + unitName, null)))
			units.get(unitName).get.init
			nextLayer ! RegisterInputUnitMsg(units.get(unitName).get.toString)
		}

		//add bias unit
		//units.put(biasUnit.toString, biasUnit)
		biasUnit = new BiasNeuronUnit(bpNeuronNetworksSetup.numInstance, nextLayer)
		biasUnit.init
	}
}