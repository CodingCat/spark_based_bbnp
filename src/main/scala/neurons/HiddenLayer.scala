package bpnn.neurons

import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.HashMap

import java.lang.String

import spark.SparkEnv
import spark.SparkContext
import SparkContext._
import spark.RDD

import bpnn._
import bpnn.utils.LayerConf

class HiddenLayer(confPath:String, layerName:String, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, layerName:String, sEnv) {
	
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
				case "init" =>
					init()
				case RegisterInputUnitMsg(srcUnitName, dstUnitName) =>
					{
						if (srcUnitName.length >= 8){
							if (srcUnitName.substring(0, 8).equals("biasUnit")) {
								//it's bias unit, so we should register it for every neuron
								//unit in this layer
								units.foreach(t2 => 
									{
										println("register biasUnit to " + t2._1)
										t2._2.registerInputUnits(srcUnitName)
									}
								)
							}
						}
						else {
							println("register " + srcUnitName + " to " + dstUnitName)
							units.get(dstUnitName).get.registerInputUnits(srcUnitName)
						}
					}
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
				case PrevLayerReadyMsg(readyLayer) =>
					readyLayer ! "init"
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
		//add bias unit
	//	biasUnit = new BiasNeuronUnit(nextLayer)
	//	biasUnit.init
		//biasUnit.run
		prevLayer.start
		prevLayer ! "start"
	} 

}