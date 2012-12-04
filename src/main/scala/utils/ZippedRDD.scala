package bpnn.utils

import spark.Dependency
import spark.OneToOneDependency
import spark.RDD
import spark.SparkContext
import spark.Split

private class ZippedSplit[InputRDDType](
    idx: Int, 
    rdd1: RDD[InputRDDType],
    rdd2: RDD[InputRDDType],
    split1: Split,
    split2: Split)
  extends Split
  with Serializable {
  
  def iterator(): Iterator[(InputRDDType, InputRDDType)] = {
    rdd1.iterator(split1).zip(rdd2.iterator(split2))
  }

  def preferredLocations(): Seq[String] =
    rdd1.preferredLocations(split1).intersect(rdd2.preferredLocations(split2))

  override val index: Int = idx
}

class ZippedRDD[InputRDDType](
    sc: SparkContext,
    @transient private val rdd1: RDD[InputRDDType],
    @transient private val rdd2: RDD[InputRDDType])
  extends RDD[(InputRDDType, InputRDDType)](sc)
  with Serializable {

  @transient
  val splits_ : Array[Split] = {
    if (rdd1 != null && rdd2 != null && rdd1.splits.size != rdd2.splits.size) {
      throw new IllegalArgumentException(
        "Can't zip RDDs with unequal numbers of partitions, where rdd1 = " + rdd1.splits.size + 
        " rdd2 = " + rdd2.splits.size)
    }
    val array = new Array[Split](rdd1.splits.size)
  
    for (i <- 0 until rdd1.splits.size) {
      array(i) = new ZippedSplit(i, rdd1, rdd2, rdd1.splits(i), rdd2.splits(i))
    }
  
    array
  }

  override def splits = splits_

  @transient
  override val dependencies = List(new OneToOneDependency(rdd1), new OneToOneDependency(rdd2))
  
  override def compute(s: Split): Iterator[(InputRDDType, InputRDDType)] = 
    s.asInstanceOf[ZippedSplit[InputRDDType]].iterator()

  override def preferredLocations(s: Split): Seq[String] =
    s.asInstanceOf[ZippedSplit[InputRDDType]].preferredLocations()

  def getRDD1:RDD[InputRDDType] = rdd1;
  def getRDD2:RDD[InputRDDType] = rdd2;
}