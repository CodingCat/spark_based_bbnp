package bpnn.neurons

import scala.xml._
import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.LinkedList
import scala.collection.mutable.HashMap
import scala.util.Random

import spark.SparkContext
import SparkContext._

import bpnn.utils._

class InputLayer(confPath:String, nextL:Actor) extends Actor {
	private var numNeurons:Int = 0
	private val conf:LayerConf = new LayerConf(confPath)
	private val units:HashMap[Int, InputNeuronUnit] = HashMap[Int, InputNeuronUnit]()
	private val nextLayer:Actor = nextL

	class InputNeuronUnit(id:Int)//, inRDDname:String, outRDDName:String) 
		extends NeuronUnit {
		
		var inputPath:String = ""
		var numinputSplit:Int = 1
		var nextLayer:Actor = nextL
		neuronId = id

		override def init(){
			val neuronIdStr:String = neuronId.toString()
			inputPath = conf.getString(
				(new StringBuilder("InputLayer.inputPath.unit" + neuronIdStr)).toString(),
				 "all")
			outputRDDName = conf.getString(
				(new StringBuilder("InputLayer.outputRDD.unit" + neuronIdStr)).toString(), 
				"all")
			numinputSplit = conf.getInt(
				(new StringBuilder("InputLayer.numinputSplit.unit" + neuronIdStr)).toString(),
				1)
		}

		override def run() {
			//build spark context
			if (globalConf.getString("global.Env.MasterURI", "local") == "local") {
				sc = new SparkContext(
					globalConf.getString("global.Env.MasterURI", "local[1]"),
	 			 	"inputLayer-Unit" + neuronId + "-generateRDD")			
			}
			else {
				if (globalConf.getString("global.Env.MasterURI", "local") == "cluster") {
					sc = new SparkContext(globalConf.getString("global.Env.MasterURI", "local"),
		 			 	"inputLayer-Unit" + neuronId + "-generateRDD", 
		 				globalConf.getString("global.Env.SparkPath", "local"),
		 				globalConf.getStringSeq("global.Env.JarURI", "local", 1))		
				}
			}
			outputRDD = sc.textFile(inputPath, numinputSplit)
			nextLayer ! (neuronId, outputRDD) 
			nextLayer ! "testMsg"
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

	def runUnits() = { 
		units.foreach((t2) => (t2._2.run())) 
	}

	// initialize the input layer
	def init() {
		//parse XML configuration file to get the number of nodes in each layer
		numNeurons = conf.getInt("InputLayer.unitNum", 1)
		println(numNeurons)
		//start numNeurons units
		for (i <- 1 to numNeurons) 
			units.put(i, new InputNeuronUnit(i))
		initUnits()
	}
}