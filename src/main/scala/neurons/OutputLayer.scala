package bpnn.neurons

import scala.collection.mutable.HashMap

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.utils.LayerConf

class OutputLayer(confPath:String, layerName:String, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, layerName, sEnv) {
	
	val units = new HashMap[String, OutputNeuronUnit]()
	
	class OutputNeuronUnit(id:Int, inputSplit:Int, inputPath:String)
		extends NeuronUnit[String, Float](id,inputSplit,inputPath) {
		
		var labelRDD:RDD[Float] = null

		override def init() {
			SparkEnv.set(sparkEnv)
			labelRDD = bpNeuronNetworksSetup.sc.
				textFile(inputPath, numInputSplit).map[Float](_.toFloat).cache()
		}

		override def run() {
			//calculate error functions
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
		numNeurons = conf.getInt("OutputLayer.unitNum", 1)
		println("OutputLayer starts " + numNeurons + " units")
		//start numNeurons units
		for (i <- 1 to numNeurons) {
			val unitName:String = "unit" + i
			units.put(unitName, 
				new OutputNeuronUnit(i, 
					conf.getInt("OutputLayer.numInputSplit." + unitName, 1),
					conf.getString("OutputLayer.inputPath." + unitName, null)))
			units.get(unitName).get.init
		}
		prevLayer.start
		prevLayer ! "start"
	}
}