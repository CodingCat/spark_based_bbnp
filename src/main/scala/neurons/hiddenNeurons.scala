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
	
	case class inputReadyMessage(id:Int, inputRDD:RDD[String])

	class HiddenNeuronUnit(id:Int)
		extends NeuronUnit {
		
		override def init(){
			
		}

		override def run() {
			//build spark context
			
		}
	}

	def act() {
		loop {
			receive {
				case "init" =>
					init()
				case inputReadyMessage(id, inputRDD) =>
					println("receive input from unit " + id)
				case "testMsg" =>
					println("haha")
					runUnits()
					println("gaga")
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