package bpnn

import spark.SparkContext
import SparkContext._

import bpnn.utils._

object bpNeuronNetworksSetup {
	var sc:SparkContext = null	
	def init() {
		if (sc == null) {
			val globalConf:LayerConf = new LayerConf("global-conf.xml")
			if (globalConf.getString("global.Env.MasterURI", "local") == "local") {
				sc = new SparkContext(
					globalConf.getString("global.Env.MasterURI", "local[1]"),
				 		"Spark-Based-BackPropogation-Neural-Networks")			
			}
			else {
				if (globalConf.getString("global.Env.MasterURI", "local") == "cluster") {
					sc = new SparkContext(globalConf.getString("global.Env.MasterURI", "local"),
		 			 	"Spark-Based-BackPropogation-Neural-Networks", 
		 				globalConf.getString("global.Env.SparkPath", "local"),
		 				globalConf.getStringSeq("global.Env.JarURI", "local", 1))		
				}
			}
		}
	}
}