package org.spark.remote

import java.net.InetAddress
import java.text.SimpleDateFormat

import akka.actor.{Actor, ActorSystem, Props}
import akka.remote.RemotingLifecycleEvent
import com.typesafe.config.ConfigFactory
import org.apache.spark.Logging
import org.spark.Message._
import org.spark.base.NodeImpl

import scala.collection.mutable
import scala.collection.mutable._
import scala.concurrent.duration._

/**
  * 使用Akka remote实现Master/Worker分布式模式,当前类为Master,主要作用接受client的请求,将提交的任务分配给已注册的Workers
  *
  * @author YuChuanQi
  * @since 2016-06-28 20:52:21
  */
class Master(host: String, port: Int) extends Actor with Logging {

  import context.dispatcher

  //已经注册的node<workId,node>
  val nodes = new HashMap[String, NodeImpl]
  //已注册的worker列表
  val workers = new HashSet[NodeImpl]
  //检测worker超时时间
  val TIME_OUT = 10 * 1000

  def createDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  @throws[Exception](classOf[Exception])
  override def preStart() = {
    logInfo("Starting Master at " + host + ":" + port)
    //context.system.eventStream.subscribe(self, classOf[RemotingLifecycleEvent])
    //周期检测节点健康情况,若超时,则认为节点丢失,从workers列表中清除该节点
    context.system.scheduler.schedule(0 millis, TIME_OUT millis, self, CheckForWorkerTimeOut)
  }

  override def receive: Receive = {
    //心跳检测
    case Heartbeat(workId) => {
      nodes.get(workId) match {
        case Some(node) =>
          node.lastHeartbeat = System.currentTimeMillis()
          val time = createDateFormat.format(node.lastHeartbeat)
          logInfo(s" ${time} : receive heartbeat from $workId")
        case None =>
          logWarning(s"Got heartbeat from unregistered worker $workId")
      }
      //sender ! "hi"
    }
    //注册worker
    case RegisterWithWorker(id, workerHost, workerPort, cores, memory) => {
      logInfo("Registering worker %s:%d with %d cores, %s M".format(workerHost, workerPort, cores, memory))
      val node = new NodeImpl(id, workerHost, workerPort, cores, memory)
      nodes += (id -> node)
      workers += node
      var w: String = ""
      workers.foreach(id => w += id + " ")
      logInfo(s"""already register workers ${w}""")
      //通知node,已经注册成功
      sender ! RegisteredWorker(s"spark://$host:$port")
    }
    //node超时检测
    case CheckForWorkerTimeOut => {
      timeOutDeadWorkers()
    }
    case _ => logInfo("Received unknow msg ")
  }

  def timeOutDeadWorkers(): Unit = {
    val currentTime = System.currentTimeMillis()
    val toRemove = workers.filter(_.lastHeartbeat < currentTime - TIME_OUT)
    for (worker <- toRemove) {
      logWarning("Removing %s because we got no heartbeat in %d seconds".format(
        worker.id, TIME_OUT / 1000))
      removeWorker(worker)
    }
  }

  def removeWorker(worker: NodeImpl) = {
    logInfo("Removing worker " + worker.id + " on " + worker.host + ":" + worker.port)
    nodes -= worker.id
    workers -= worker
    logInfo("Registered Worker : " + workers)
  }
}

object MasterActor {
  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    val host = InetAddress.getLocalHost.getHostAddress
    val port = 5150
    val akkaConf = ConfigFactory.parseString(
      s"""
         |akka.remote.netty.tcp.hostname = ${host}
         |akka.remote.netty.tcp.port = ${port}
                      """.stripMargin).withFallback(conf)
    val system = ActorSystem("MasterSystem", conf)
    val master = system.actorOf(Props(classOf[Master], host, port), name = "master")
  }
}
