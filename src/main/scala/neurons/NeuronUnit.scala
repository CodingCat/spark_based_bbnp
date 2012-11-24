package bpnn.neurons

import scala.actors.Actor
import scala.collection.mutable.HashMap
import scala.util.Random

import spark.RDD
import spark.SparkContext

import bpnn.utils.LayerConf

abstract class NeuronUnit[InputType, OutputType](
	protected var neuronId:Int,
	protected var numInputSplit:Int,
	private var inputPath:String = null
	) {
		protected var inputRDD:RDD[InputType] = null
		protected var outputRDD:RDD[OutputType] = null
		protected var numInputUnit = 0
		protected val RandomGen = new Random()
		protected val inputWeights:HashMap[String, Float] = new HashMap[String, Float]
		protected val inputReadyFlags:HashMap[String, Int] = new HashMap[String, Int]
		

		def init()
		def run()

		protected def AllInputReady():Boolean = {
			var cnt:Int = 0;
			inputReadyFlags.foreach((t2) => if (t2._2 == 1) cnt = cnt + 1)
			cnt == numInputUnit  
		}

		def registerInputUnits(unitName:String) {
			inputWeights.put(unitName, RandomGen.nextFloat())
			inputReadyFlags.put(unitName, 0)
		}

		override def toString = "unit" + neuronId
}
