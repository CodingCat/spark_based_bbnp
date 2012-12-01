package bpnn.neurons

import scala.collection.mutable.HashMap
import scala.math

import java.lang.String

import spark.SparkEnv
import spark.SparkContext
import spark.RDD
import spark.rdd.HadoopRDD

import bpnn._
import bpnn.utils.ZippedRDD
import bpnn.system.LayerCoordinator
import bpnn.utils.LayerConf

class HiddenLayer(confPath:String, layerName:String, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, layerName:String, sEnv) {
	
	val units = new HashMap[String, HiddenNeuronUnit]()
	
	class HiddenNeuronUnit(id:Int, inputSplit:Int)
		extends NeuronUnit[Float, Float](id, inputSplit) {
		
		override def init() {
			SparkEnv.set(sparkEnv)
			numInputUnit = conf.getInt("HiddenLayer.numInputUnit." + this.toString, 1)
			//1. get output unit list
			val outStr = conf.getString("HiddenLayer.OutDst." + this.toString, "unit1")
			//2. Converst outList to list
			val outList = outStr.split(',')
			for (i <- 1 to outList.length) 
				nextLayer ! RegisterInputUnitMsg(this.toString, "unit" + i)
		}

		override def run() {
			//implement the activation function here
			//take into all input dataset into consideration
			//Description of the algorithm here:
			SparkEnv.set(sparkEnv)
			val a:Array[RDD[Float]] = new Array[RDD[Float]](inputRDDList.size)
			inputRDDList.values.copyToArray(a)
			var zippedRDD:ZippedRDD[Float, Float] = null
			var mergedRDD:RDD[Float] = null
			//merge the input RDDs one by one with zippedRDD
			for (i <- 0 to a.length - 2) {
				println("here")
				if (mergedRDD == null) {
					zippedRDD = new ZippedRDD[Float, Float](bpNeuronNetworksSetup.sc, 
						a(i), a(i + 1))
				}
				else {
					zippedRDD = new ZippedRDD[Float, Float](bpNeuronNetworksSetup.sc, 
						mergedRDD, a(i + 1))
				}
				mergedRDD = zippedRDD.map(t2 => t2._1 + t2._2)
			}
			outputRDD = mergedRDD.map[Float](t => (1/(1 + math.exp(t.toDouble))).toFloat)
			println(outputRDD.count)
			resetReadyFlags()
			nextLayer ! InputUnitReadyMessage(this.toString, outputRDD)
		}



		def transformInputRDD(key:String, readyRDD:RDD[Float]) {
			SparkEnv.set(sparkEnv)
			println(key)
			println(inputWeights.get(key).get)
			if (readyRDD == null) println("bad")
			inputRDDList.put(key, readyRDD.map(inputEle => inputEle * 
							inputWeights.get(key).get))
			//println(readyRDD.count)
		}

	}

	def act() {
		loop {
			react {
				case "start" =>
					nextLayer ! PrevLayerReadyMsg(this)
				case "init" =>
					init()
					biasUnit.run
					LayerCoordinator ! LayerReadyMsg(this)
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
									t2._2.run()
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
		biasUnit = new BiasNeuronUnit(nextLayer)
		biasUnit.init
	
		prevLayer.start
		prevLayer ! "start"
	} 

}