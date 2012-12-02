package bpnn

import spark.SparkContext

import bpnn.utils._

object bpNeuronNetworksSetup {
	var sc:SparkContext = null	
	
	def init() {
		if (sc == null) {
			LayerConf.loadConfiguration("global-conf.xml")
			if (LayerConf.getString("global.Env.runningMode", "local") == "local") {
				sc = new SparkContext(
					LayerConf.getString("global.Env.MasterURI", "local[1]"),
				 		"Spark-Based-BackPropogation-Neural-Networks")			
			}
			else {
				if (LayerConf.getString("global.Env.runningMode", "local") == "cluster") {
					sc = new SparkContext(
						LayerConf.getString("global.Env.MasterURI", "spark://localhost:7070"),
		 			 	"Spark-Based-BackPropogation-Neural-Networks", 
		 				LayerConf.getString("global.Env.SparkPath", "local"),
		 				LayerConf.getStringSeq("global.Env.JarURI", "local", 1))		
				}
			}
		}
	}
}