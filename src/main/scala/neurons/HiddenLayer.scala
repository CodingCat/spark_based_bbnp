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
	
	val	units = HashMap[Int, HiddenNeuronUnit]()
	
	class HiddenNeuronUnit(id:Int, inputSplit:Int)
		extends NeuronUnit[Float, Float](id, inputSplit) {
		
		override def run() {
			//build spark context
			
		}
	}

	def act() {
		loop {
			react {
				case "init" =>
					init()
				case inputUnitReadyMessage(id, inputRDD) =>
					if (id != -1) {
						println("receive input from unit " + id)
					}
					else {
						println("receive input from bias unit")
					}
				case testMsgClass(id) =>
					println("hello world")
			}
		}
	}
	
	override def runUnits() {
		SparkEnv.set(sparkEnv)
		bpNeuronNetworksSetup.sc.textFile("input_data_set/input_unit1.dat", 1)
	}	

	override def init() {
		
	}
}