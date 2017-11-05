package org.spark.base

/**
  *
  * @author YuChuanQi
  * @since 2016-06-28 20:52:21
  */
class NodeImpl(val id: String, val host: String, val port: Int, val cores: Int, val memory: Int) {
  var lastHeartbeat: Long = _

  init()

  def init() = {
    lastHeartbeat = System.currentTimeMillis()
  }

  override def toString: String = {
    s"""{${id}:<cores:$cores,memory:$memory>}"""
  }
}
