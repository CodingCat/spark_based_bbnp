package bpnn.neurons

import scala.actors._

class biasUnit extends Actor{
	def act() {
		react {
			//cases;
			case "HiddenLayer" => 
				println("send bias to Hidden Layer")
				act()
			case "OutputLayer" =>
				println("send bias to Output Layer")
				act()
		}
	}
}