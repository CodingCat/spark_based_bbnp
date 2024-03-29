package bpnn.neurons

import scala.collection.mutable.HashMap

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.utils._
import bpnn.system.LayerCoordinator
import neuronunits._

class OutputLayer(
	confPath:String, layerName:String, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, layerName, sEnv) with Logging {
	
	private val units = new HashMap[String, OutputNeuronUnit]()
	private var Error:Long = 0;
	def act() {
		loop {
			react {
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
								if (t2._2.AllInputReady) {
									println(t2._1 + "'s inputs are ready")
									t2._2.run()
									//send the derivative list to prev layer
									prevLayer ! DeriveListReadyMsg(t2._2.toString, 
										t2._2.DeriveList)
									//send RoundFinishMsg to Coordinator
									LayerCoordinator ! RoundFinishMsg(this.toString)
								}
							}
						}
					)
				case PrevLayerReadyMsg(readyLayer) =>
					readyLayer ! "init"
			}
		}
	}

	//initialize the input layer
	override def init() {
		//parse XML configuration file to get the number of nodes in each layer
		val numInputSplits:Int = conf.getInt("OutputLayer.numInputSplits", 1)
		val numInputUnits:Int  = conf.getInt("OutputLayer.numInputUnits", 1)
		SparkEnv.set(sparkEnv)
		numNeurons = conf.getInt("OutputLayer.unitNum", 1)
		println("OutputLayer starts " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) {
			val unitName:String = "unit" + i
			units.put(unitName, 
				new OutputNeuronUnit(i, 
					numInputSplits,
					numInputUnits,
					conf.getString("OutputLayer.inputPath." + unitName, null)))
			units.get(unitName).get.init
		}
		prevLayer.start
		prevLayer ! StartPrevLayerMsg
	}
}