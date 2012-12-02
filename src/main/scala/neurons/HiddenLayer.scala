package bpnn.neurons

import scala.collection.mutable.HashMap
import scala.math

import java.lang.String

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.utils._
import bpnn.system.LayerCoordinator
import neuronunits._

class HiddenLayer(
	confPath:String, 
	layerName:String, 
	sEnv:SparkEnv) 
	extends NeuronLayer(confPath:String, layerName:String, sEnv) {
	
	private val units = new HashMap[String, HiddenNeuronUnit]()
	
	def act() {
		loop {
			react {
				case "start" =>
					nextLayer ! PrevLayerReadyMsg(this)
				case "init" =>
					init()
					if (biasUnit.run() == true) {
						nextLayer ! InputUnitReadyMessage(biasUnit.toString, biasUnit.getOutput) 
					}
					LayerCoordinator ! LayerReadyMsg(this)
				case RegisterInputUnitMsg(srcUnitName) =>
					{
						// only support fully connected networks
						units.foreach(t2 => t2._2.registerInputUnits(srcUnitName))
					}
				case InputUnitReadyMessage(readyUnit, readyRDD) =>
					units.foreach(
						t2 => 
						{
							if (t2._2.hasThisInputUnit(readyUnit)) {
								println(readyUnit + "  is ready for " + t2._2.toString)
								t2._2.markReadyUnit(readyUnit)
								//transform readyRDD by multiply it with weights
								t2._2.transformInputRDD(readyUnit, readyRDD)
								if (t2._2.AllInputReady()) {
									if (t2._2.run() == true) {
										nextLayer ! InputUnitReadyMessage(t2._2.toString, t2._2.getOutput)
									}
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
		val numInputSplits:Int = conf.getInt("HiddenLayer.numInputSplits", 1)
		numNeurons = conf.getInt("HiddenLayer.unitNum", 1)
		println("Hidden Layer starts " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) {
			val unitName:String = "unit" + i
			units.put(unitName, 
				new HiddenNeuronUnit(i, 
					numInputSplits))
			units.get(unitName).get.init()
			nextLayer ! RegisterInputUnitMsg(units.get(unitName).get.toString)
		}
		//add bias unit
		biasUnit = new BiasNeuronUnit(bpNeuronNetworksSetup.numInstance, nextLayer)
		biasUnit.init
	
		prevLayer.start
		prevLayer ! "start"
	} 

}