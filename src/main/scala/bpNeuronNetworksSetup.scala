package bpnn

import spark.SparkContext

import bpnn.utils._

object bpNeuronNetworksSetup {
	var sc:SparkContext = null	
	private val conf:LayerConf = new LayerConf("global-conf.xml")

	def init() {
		if (sc == null) {
			if (conf.getString("global.Env.runningMode", "local") == "local") {
				sc = new SparkContext(
					conf.getString("global.Env.MasterURI", "local[1]"),
				 		"Spark-Based-BackPropogation-Neural-Networks")			
			}
			else {
				if (conf.getString("global.Env.runningMode", "local") == "cluster") {
					sc = new SparkContext(
						conf.getString("global.Env.MasterURI", "spark://localhost:7070"),
		 			 	"Spark-Based-BackPropogation-Neural-Networks", 
		 				conf.getString("global.Env.SparkPath", "local"),
		 				conf.getStringSeq("global.Env.JarURI", "local", 1))		
				}
			}
		}
	}

	def numInstance:Int = conf.getInt("global.TrainingSet.Num", 200);
	def numIterations:Int = conf.getInt("global.IterationNum", 5);
}