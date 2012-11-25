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
			numInputUnit = conf.getInt("HiddenLayer.numInputUnit." + this.toString, 1)
		}

		override def run() {
			//build spark context
			println(this.toString + " is executing")
		}
	}

	def act() {
		loop {
			react {
				case "initializeUnits" =>
					init()
				case RegisterInputUnitMsg(srcUnitName, dstUnitName) =>
					println("register " + srcUnitName + " to " + dstUnitName)
					units.get(dstUnitName).get.registerInputUnits(srcUnitName)
				case InputUnitReadyMessage(readyUnit, inputRDD) =>
					units.foreach(
						t2 => 
						{
							if (t2._2.hasThisInputUnit(readyUnit)) {
								println(readyUnit + "  is ready for " + t2._2.toString)
								t2._2.markReadyUnit(readyUnit)
								if (t2._2.AllInputReady()) {
									t2._2.run()
									t2._2.resetReadyFlags()
								}
							}
						}
					)
			}
		}
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