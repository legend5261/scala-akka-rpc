package org.spark.remote

import java.io.File
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{ActorSelection, Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.apache.spark.Logging
import org.spark.Message._
import scala.concurrent.duration._

/**
  * 使用Akka remote实现Master/Worker分布式模式,当前类为Work,主要作用处理具体的任务,向Master汇报资源使用情况,发送心跳等
  *
  * @author YuChuanQi
  * @since 2016-06-28 20:52:21
  */
class Worker(host: String,
             port: Int,
             cores: Int,
             memory: Int) extends Actor with Logging {

  import context.dispatcher

  def createDateFormat = new SimpleDateFormat("yyyyMMddHHmmss")

  // For worker and executor IDs
  val workerId = generateWorkerId()
  var master: ActorSelection = null
  @volatile var registed = false
  @volatile var connected = false

  def generateWorkerId(): String = {
    "worker-%s-%s-%d".format(createDateFormat.format(new Date), host, port)
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    println("connect to master akka.tcp://MasterSystem@127.0.0.1:5150/user/master ")
    master = context.actorSelection("akka.tcp://MasterSystem@127.0.0.1:5150/user/master")
    master ! RegisterWithWorker(workerId, host, port, cores, memory)
  }

  def changeMaster() = {
    if (master == null) {
      master = context.actorSelection("akka.tcp://MasterSystem@127.0.0.1:5150/user/master")
    }
    connected = true
  }

  override def receive: Receive = {
    case RegisteredWorker(url) => {
      registed = true
      logInfo(s"Successfully registered with Master $url")
      context.system.scheduler.schedule(0 millis, 5000 millis, self, SendHeartbeat)
    }
    case SendHeartbeat =>
      changeMaster()
      if (connected)
        master ! Heartbeat(workerId)
    case _: String => {
      println("get message from master ")
    }
  }
}

object WorkerActor {
  def main(args: Array[String]) {
    val confFile = getClass.getClassLoader.getResource("application.conf").getFile
    val conf = ConfigFactory.parseFile(new File(confFile))
    val port = 1234
    val host = InetAddress.getLocalHost.getHostAddress
    val cores = 4
    val memory = 4096

    val akkaConf = ConfigFactory.parseString(
      s"""
         |akka.remote.netty.tcp.hostname = ${host}
         |akka.remote.netty.tcp.port = ${port}""".stripMargin).withFallback(conf)
    val system = ActorSystem("WorkSystem", akkaConf)
    val work = system.actorOf(Props(classOf[Worker], host, port, cores, memory), name = "work")
  }
}

