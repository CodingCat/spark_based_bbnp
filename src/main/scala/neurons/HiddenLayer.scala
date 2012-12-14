package bpnn.neurons

import scala.collection.mutable.HashMap
import scala.math
import scala.reflect.Manifest

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
	extends NeuronLayer(confPath:String, layerName:String, sEnv) with Logging {
	
	private val units = new HashMap[String, HiddenNeuronUnit]()
	private var cntUpdatedWeightsUnits = 0

	def act() {
		loop {
			react {
				case StartPrevLayerMsg =>
					nextLayer ! PrevLayerReadyMsg(this)
				case "init" =>
					init()

					LayerCoordinator ! LayerReadyMsg(this)
				case RegisterInputUnitMsg(srcUnitName) =>
					{
						// only support fully connected networks
						units.foreach(t2 => t2._2.registerInputUnits(srcUnitName))
					}
				case InputUnitReadyMessage(readyUnit, readyRDD) =>
					units.foreach(
						t2 => {
							if (t2._2.hasThisInputUnit(readyUnit)) {
								println(readyUnit + "  is ready for " + t2._2.toString)
								t2._2.markReadyUnit(readyUnit)
								//transform readyRDD by multiply it with weights
								t2._2.transformInputRDD(readyUnit, readyRDD)
								if (t2._2.AllInputReady()) {
									if (t2._2.run() == true) {
										nextLayer ! InputUnitReadyMessage(t2._2.toString, t2._2.getOutput)
										t2._2.resetReadyFlags()
									}
								}
							}
						}
					)
					if (biasUnit.run() == true) {
						nextLayer ! InputUnitReadyMessage(biasUnit.toString, 
							biasUnit.getOutput) 
					}
				case DeriveListReadyMsg(outUnit:String, 
					readyRDDMap:HashMap[String, RDD[Float]]) =>
					//logInfo("receive DeriveListReadyMsg ")
					readyRDDMap.foreach(t2 => {
							println(t2._1 + "\'s derive is ready");
							if (t2._1.substring(0, 4) != "bias") {
								units.get(t2._1).get.receiveDerive(outUnit, t2._2)
								if (nextLayer.LayerSize == units.get(t2._1).get.CntReceiveGradient) {
									units.get(t2._1).get.updateWeights()
									cntUpdatedWeightsUnits = cntUpdatedWeightsUnits + 1
									prevLayer ! DeriveListReadyMsg(t2._2.toString, 
										units.get(t2._1).get.DeriveList)
								}
							}
						}
					)
					if (RoundCompleted) {
						//reset counters
						units.foreach(t2 => t2._2.reset)
						LayerCoordinator ! RoundFinishMsg(this.toString)
					}
				case PrevLayerReadyMsg(readyLayer) =>
					readyLayer ! "init"
			}
		}
	}

	private def RoundCompleted():Boolean = {
		units.foreach(t2 => {
				if (t2._2.CntReceiveGradient != nextLayer.LayerSize) {
					println(t2._1 + ":" + t2._2.CntReceiveGradient + ":" + nextLayer.LayerSize)
					return false
				}
			}
		)
		true
	}
	
	override def init() {
		//parse XML configuration file to get the number of nodes in each layer
		val numInputSplits:Int = conf.getInt("HiddenLayer.numInputSplits", 1)
		val numInputUnits:Int  = conf.getInt("HiddenLayer.numInputUnits", 1)
		numNeurons = conf.getInt("HiddenLayer.unitNum", 1)
		println("Hidden Layer starts " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) {
			val unitName:String = "unit" + i
			units.put(unitName, 
				new HiddenNeuronUnit(i, 
					numInputSplits, 
					numInputUnits))
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