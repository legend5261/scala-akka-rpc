/*
package org.spark.utils

import java.io.File
import java.net.InetAddress
import java.util.Properties

import akka.actor.{ActorSystem, Address, ExtendedActorSystem}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._

object AkkaUtils {

  def createActorSystem(name: String, conf: Config): (ActorSystem, String, Int) = {
    val actorSystem = ActorSystem(name, conf)
    val host = InetAddress.getLocalHost.getHostAddress
    val provider = actorSystem.asInstanceOf[ExtendedActorSystem].provider
    val boundPort = provider.getDefaultAddress.port.getOrElse(conf.getInt("akka.remote.netty.tcp.port"))
    (actorSystem, host, boundPort)
  }

  def createActorSystem(name: String, conf: Map[String, String], classPathConfigFileName: String): (ActorSystem, String, Int) = {
    val akkaConf = ConfigFactory.parseMap(conf).withFallback(ConfigFactory.load(classPathConfigFileName))
    createActorSystem(name, akkaConf)
  }

  /**
   * 改变其端口的ActorSystem
   */
  def createActorSystem(name: String, port: Int, classPathConfigFileName: String): (ActorSystem, String, Int) = {
    val akkaConf = ConfigFactory.parseString( s"""
                                                 |akka.remote.netty.tcp.hostname = ${InetAddress.getLocalHost.getHostAddress}
        |akka.remote.netty.tcp.port = ${port}
                      """.stripMargin).withFallback(ConfigFactory.load(classPathConfigFileName))
    createActorSystem(name, akkaConf)
  }

  def createActorSystem(name: String, port: Int, file: File): (ActorSystem, String, Int) = {
    val akkaConf = ConfigFactory.parseString( s"""
                                                 |akka.remote.netty.tcp.hostname = ${InetAddress.getLocalHost.getHostAddress}
        |akka.remote.netty.tcp.port = ${port}
                      """.stripMargin).withFallback(ConfigFactory.parseFile(file))
    createActorSystem(name, akkaConf)
  }

  def createActorSystem(name: String, conf: Properties, classPathConfigFileName: String): (ActorSystem, String, Int) = {
    createActorSystem(name, conf.toMap, classPathConfigFileName)
  }

  def getActorSystemAndHostPort(actorSystemName:String,port:Int,conf:Configuration):(ActorSystem,String,Int) = {
    val (isUseEnv,fileLocation) = conf.getApplicationConfFile
    if(isUseEnv){
      AkkaUtils.createActorSystem(actorSystemName, port, new File(fileLocation))
    }else{
      AkkaUtils.createActorSystem(actorSystemName, port, fileLocation)
    }
  }

  /**
   * load system env
   */
  def createActorSystem(name: String, classPathConfigFileName: String): (ActorSystem, String, Int) = {
    val fileName = sys.env.getOrElse("ENV_AKKA_CONF", "env_akka_conf");
    val akkaConf = ConfigFactory.parseFile(new File(fileName)).withFallback(ConfigFactory.load(classPathConfigFileName))
    createActorSystem(name, akkaConf)
  }

  def protocol(actorSystem: ActorSystem): String = {
    val akkaConf = actorSystem.settings.config
    val sslProp = "akka.remote.netty.tcp.enable-ssl"
    protocol(akkaConf.hasPath(sslProp) && akkaConf.getBoolean(sslProp))
  }

  def protocol(ssl: Boolean = false): String = {
    if (ssl) {
      "akka.ssl.tcp"
    } else {
      "akka.tcp"
    }
  }

  def address(
               protocol: String,
               systemName: String,
               hostPort: String,
               actorName: String): String = {
    s"$protocol://$systemName@$hostPort/user/$actorName"
  }

  def toAkkaAddress(url: String, protocol: String, systemName: String): Address = {
    val hostPort = url.split(":")
    Address(protocol, systemName, hostPort(0), hostPort(1).toInt)
  }

  def toAkkaUrl(url: String, systemName: String, protocol: String, actorName: String): String = {
    AkkaUtils.address(protocol, systemName, url, actorName)
  }
}*/
