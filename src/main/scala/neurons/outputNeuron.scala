package bpnn.neurons

import scala.actors.Actor
import scala.actors.Actor._

class outputNeuron extends Actor {
	private def predictError = 0
	//private val predictResult = Map[Int, Int]()
	//private val expectedResult = List[Int]()
	def act() {
		init()
		react {
			case "endOfIteration" =>
				getPredictError()
				act()
			case "updateInstanceError" =>
				updateInstanceError()
				act()
		}
	}

	def init() {
		//load the expected result
	}

	def updateInstanceError() {

	}

	def getPredictError() {

	}
}