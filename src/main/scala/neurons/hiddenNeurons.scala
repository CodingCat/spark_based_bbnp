package bpnn.neurons

import scala.actors.Actor
import scala.actors.Actor._

import spark.SparkContext
import SparkContext._
import spark.RDD


class HiddenLayer(confPath:String, nextL:Actor) extends Actor {

	private val numNeurons = 0
	private val nextLayer:Actor = nextL

	def act() {
		loop {
			react {
				case (id:Int, inputRDD:RDD[String]) =>
					println("receive input from unit " + id)
				case "testMsg" =>
					println("haha")
			}
		}
	}
	
	def init() {
		
	}
}