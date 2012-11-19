package bpnn.utils

import scala.xml._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArraySeq
import scala.collection.mutable.StringBuilder

class LayerConf(confPath:String) {
	//constructor
	private val confFile = XML.loadFile(confPath)
	private val propertyMap:HashMap[String, String] = new HashMap[String, String]()

	for (property <- confFile \\ "property") {
		propertyMap.put((property \\ "name").text,  (property \\ "value").text)
	}

	def getInt(propertyName:String, defaultValue:Int):Int = {
		if (propertyMap.contains(propertyName) == true) {
			propertyMap.get(propertyName).get.toInt
		}
		else {
			defaultValue
		}
	}

	def getString(propertyName:String, defaultValue:String):String = {
		propertyMap.getOrElse(propertyName, defaultValue).asInstanceOf[String]
	}

	def getStringSeq(propertyName:String, defaultValue:String, Seqlen:Int):Seq[String] = {
		if (propertyMap.contains(propertyName) == true) {
			//propertyMap.get(propertyName).get.toInt
			val strArray:Array[String] = (new StringBuilder(
				propertyMap.get(propertyName).get)).split(',')
			var strSeq:ArraySeq[String] = ArraySeq[String]()
			strArray.foreach((str:String) => (strSeq = strSeq :+ str))
			strSeq
		}
		else {
			var strSeq:ArraySeq[String] = ArraySeq[String]()
			for (i <- 1 to Seqlen) {
				strSeq  = strSeq :+ defaultValue
			}
			strSeq
		}
	} 
}	