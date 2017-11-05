package org.spark

import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by pc on 2016/7/1.
 */
object TestReadFile {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("TestSpark").setMaster("local")
    val sc = new SparkContext(conf)
    val file = sc.textFile("file/aaa")
    //val result = file.groupBy(s => s.split(" ").length)//.flatMapValues(res => res.toList)
    val result = file.map(a => a.length)
    result.collect().foreach(println)
    var reduceResult = result.reduce((a, b) => a+b)
    println(reduceResult)
  }
}
