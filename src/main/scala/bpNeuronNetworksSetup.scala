package bpnn

import spark.SparkContext

import bpnn.utils._

object bpNeuronNetworksSetup {
	var sc:SparkContext = null	
	val globalConf:LayerConf = new LayerConf("global-conf.xml")

	def init() {
		if (sc == null) {
			if (globalConf.getString("global.Env.runningMode", "local") == "local") {
				sc = new SparkContext(
					globalConf.getString("global.Env.MasterURI", "local[1]"),
				 		"Spark-Based-BackPropogation-Neural-Networks")			
			}
			else {
				if (globalConf.getString("global.Env.runningMode", "local") == "cluster") {
					sc = new SparkContext(
						globalConf.getString("global.Env.MasterURI", "spark://localhost:7070"),
		 			 	"Spark-Based-BackPropogation-Neural-Networks", 
		 				globalConf.getString("global.Env.SparkPath", "local"),
		 				globalConf.getStringSeq("global.Env.JarURI", "local", 1))		
				}
			}
		}
	}
}