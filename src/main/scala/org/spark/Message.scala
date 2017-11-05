package org.spark

/**
  *
  * @author YuChuanQi
  * @since 2016-06-28 20:52:21
  */
//sealed关键字说明 接口trait Message的子类必须和Message在同一个文件中,如下边的HeartBeat类是Message的子类
sealed trait Message extends Serializable

object Message {

  case class Heartbeat(workerId: String) extends Message

  case class RegisterWithWorker(
                                 id: String,
                                 host: String,
                                 port: Int,
                                 cores: Int,
                                 memory: Int) extends Message

  case class RegisteredWorker(url: String) extends Message

  case object CheckForWorkerTimeOut

  case object SendHeartbeat

}
