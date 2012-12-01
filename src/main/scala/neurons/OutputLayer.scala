package bpnn.neurons

import scala.collection.mutable.HashMap

import spark.SparkEnv
import spark.SparkContext
import spark.RDD

import bpnn._
import bpnn.utils._
import bpnn.system.LayerCoordinator

class OutputLayer(confPath:String, layerName:String, sEnv:SparkEnv) 
	extends NeuronLayer(confPath, layerName, sEnv) {
	
	val units = new HashMap[String, OutputNeuronUnit]()
	
	class OutputNeuronUnit(id:Int, inputSplit:Int, inputPath:String)
		extends NeuronUnit[Float, Float](id,inputSplit,inputPath) {
		
		private var labelRDD:RDD[Float] = null

		override def init() {
			SparkEnv.set(sparkEnv)
			numInputUnit = conf.getInt("OutputLayer.numInputUnit." + this.toString, 1)
			labelRDD = bpNeuronNetworksSetup.sc.
				textFile(inputPath, numInputSplit).map[Float](_.toFloat).cache()
		}

		override def run() {
			//get the final result

			println("final result run")
			val a:Array[RDD[Float]] = new Array[RDD[Float]](inputRDDList.size)
			inputRDDList.values.copyToArray(a)
			var zippedRDD:ZippedRDD[Float, Float] = null
			var mergedRDD:RDD[Float] = null
			println(a.length - 2)
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
			outputRDD = mergedRDD.map(t => 1/(1 + math.exp(t.toDouble).toFloat))
			//outputRDD.saveAsTextFile("result.txt")
			//println(outputRDD.count)
			resetReadyFlags()
		}

		def transformInputRDD(key:String, readyRDD:RDD[Float]) {
			inputRDDList.put(key, readyRDD.map(inputEle => inputEle * 
							inputWeights.get(key).get))
		}
	}

	def act() {
		loop {
			react {
				case "init" =>
					init()
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
								if (t2._2.AllInputReady) {
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