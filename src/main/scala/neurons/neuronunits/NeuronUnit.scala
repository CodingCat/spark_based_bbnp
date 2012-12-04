package bpnn.neurons.neuronunits

import scala.actors.Actor
import scala.collection.mutable.HashMap
import scala.util.Random

import spark.RDD
import spark.SparkEnv
import spark.SparkContext

import bpnn._
import bpnn.utils._
import bpnn.neurons._
import bpnn.system._

@SerialVersionUID(10L)
abstract class NeuronUnit[InputType, OutputType](
	protected var neuronId:Int,
	protected var numInputSplit:Int,
	protected val numInputUnit:Int, 
	private var inputPath:String = null
	) extends Serializable with Logging {

		protected var inputRDDList:HashMap[String, RDD[InputType]] = 
			new HashMap[String, RDD[InputType]]
		protected var outputRDD:RDD[OutputType] = null
		protected var inputWeights:HashMap[String, Float] = new HashMap[String, Float]
		protected var inputReadyFlags:HashMap[String, Int] = new HashMap[String, Int]
		
		
		def init(){

		}

		def run():Boolean

		def AllInputReady():Boolean = {
			var cnt:Int = 0;
			inputReadyFlags.foreach((t2) => if (t2._2 == 1) cnt = cnt + 1)
			println(cnt + ":" + numInputUnit)
			cnt == numInputUnit  
		}

		def resetReadyFlags() { inputReadyFlags.foreach(
			(t2) => inputReadyFlags(t2._1) = 0) }

		def markReadyUnit(readyUnit:String) {
			inputReadyFlags(readyUnit) = 1
		}

		def registerInputUnits(unitName:String) {
			inputWeights.put(unitName, 0.01f)
			inputReadyFlags.put(unitName, 0)
		}

		def hasThisInputUnit(candidate:String):Boolean = inputWeights.contains(candidate)

		def getOutput = outputRDD
		
		override def toString = "unit" + neuronId
}
