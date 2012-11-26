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
import bpnn.utils._

class InputLayer(confPath:String, layerName:String, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, layerName, sEnv) {
	
	val units = new HashMap[String, InputNeuronUnit]()
	
	class InputNeuronUnit(id:Int, inputSplit:Int, inputPath:String)
		extends NeuronUnit[String, Float](id,inputSplit,inputPath) {
		
		override def init() {
			//send out register messages
			//1. get output unit list
			val outStr = conf.getString("InputLayer.OutDst." + this.toString, "unit1")
			//2. Converst outList to list
			val outList = outStr.split(',')
			for (i <- 1 to outList.length) 
				nextLayer ! RegisterInputUnitMsg(this.toString, "unit" + i)
		}

		override def run() {
			SparkEnv.set(sparkEnv)
			outputRDD = bpNeuronNetworksSetup.sc.
				textFile(inputPath, numInputSplit).map[Float](_.toFloat).cache()
			nextLayer ! InputUnitReadyMessage(this.toString, outputRDD) 
		}
	}

	def act() {
		loop {
			react {
				case "start" =>
					nextLayer ! PrevLayerReadyMsg(this)
				case "init" =>
					init()
				case "run" =>
					println("start run")
					runUnits()
			}
		}
	}

	
	override def runUnits() {
		units.foreach(
			t2 => 
			{
				println(t2._2 + " is running")
				t2._2.run()
			}
		) 
		biasUnit.run
	}

	//initialize the input layer
	override def init() {
		//parse XML configuration file to get the number of nodes in each layer
		SparkEnv.set(sparkEnv)
		numNeurons = conf.getInt("InputLayer.unitNum", 1)
		println("InputLayer starts " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) {
			val unitName:String = "unit" + i
			units.put(unitName, 
				new InputNeuronUnit(i, 
					conf.getInt("InputLayer.numInputSplit." + unitName, 1),
					conf.getString("InputLayer.inputPath." + unitName, null)))
			units.get(unitName).get.init
		}

		//add bias unit
		//units.put(biasUnit.toString, biasUnit)
		biasUnit = new BiasNeuronUnit(nextLayer)
		biasUnit.init
	}
}