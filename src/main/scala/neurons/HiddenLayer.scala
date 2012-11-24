package bpnn.neurons

import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.HashMap

import spark.SparkEnv
import spark.SparkContext
import SparkContext._
import spark.RDD

import bpnn._
import bpnn.utils.LayerConf

class HiddenLayer(confPath:String, nextL:Actor, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, nextL, sEnv) {
	
	val units = new HashMap[String, HiddenNeuronUnit]()
	
	class HiddenNeuronUnit(id:Int, inputSplit:Int)
		extends NeuronUnit[Float, Float](id, inputSplit) {
		
		override def init() {

		}

		override def run() {
			//build spark context
			
		}
	}

	def act() {
		loop {
			react {
				case "initializeUnits" =>
					init()
				case RegisterInputUnitMsg(srcUnitName, dstUnitName) =>
					println("register " + srcUnitName + " to " + dstUnitName)
					targetUnit:HiddenNeuronUnit = units.get(dstUnitName).get
					targetUnit.get.registerInputUnits(srcUnitName)
				case InputUnitReadyMessage(id, inputRDD) =>
					if (id != -1) {
						println("receive input from unit " + id)
					}
					else {
						println("receive input from bias unit")
					}
					//pipeline the processing
					if (targetUnit.get(dstUnitName).get.AllInputReady()) targetUnit.run()
			}
		}
	}
	
	override def runUnits() {
		SparkEnv.set(sparkEnv)
		bpNeuronNetworksSetup.sc.textFile("input_data_set/input_unit1.dat", 1)
	}	

	override def init() {
		//parse XML configuration file to get the number of nodes in each layer
		numNeurons = conf.getInt("HiddenLayer.unitNum", 1)
		println("Hidden Layer starts " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) {
			val unitName:String = "unit" + i
			units.put(unitName, 
				new HiddenNeuronUnit(i, 
					conf.getInt("HiddenLayer.numInputSplit." + unitName, 1)))
			units.get(unitName).get.init()
		}
	} 
}