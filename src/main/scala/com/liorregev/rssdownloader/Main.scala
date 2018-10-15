package com.liorregev.rssdownloader

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.liorregev.rssdownloader.reader.RssReader
import com.liorregev.rssdownloader.store.{MySQLDownloadsStore, StoredTorrent}
import com.liorregev.rssdownloader.transmission.Client
import com.liorregev.rssdownloader.transmission.domain.{AddTorrentRequest, AddTorrentResponse, AddTorrentResult, TorrentSource}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.StdIn

object Main extends App {
  final case class Config(rssURL: String = "", serverIP: String = "")

  private val parser = new scopt.OptionParser[Config]("scopt") {
    head("scopt", "3.x")

    opt[String]('r', "rssURL")
      .required()
      .action( (x, c) => c.copy(rssURL = x) )
      .text("The RSS feed URL")

    opt[String]('s', "serverIP")
      .required()
      .action( (x, c) => c.copy(serverIP = x) )
      .text("The IP for the MySQL db and Transmission")
  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      implicit val system: ActorSystem = ActorSystem()
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      implicit val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

      val rssReader = new RssReader(config.rssURL)
      val txClient = new Client(s"http://${config.serverIP}:9092/transmission/rpc/")
      val store = new MySQLDownloadsStore(s"jdbc:mysql://${config.serverIP}:3306/torrents")

      sys.addShutdownHook({
        wsClient.close()
        system.terminate()
      })

      system.scheduler.schedule(Duration.Zero, 1 hour, new Runnable {
        override def run(): Unit = {
          val result = for {
            items <- rssReader.read()
            queriedItems <- Future.sequence(items.map(item => store.containsItem(item).map(item -> _)))
            responses <- Future.sequence(queriedItems.filterNot(_._2).map(_._1)
              .map(item => txClient.request(AddTorrentRequest(TorrentSource.Filename(item.link))).map(item -> _)))
            storeResults <- Future.sequence(responses.filter(_._2.isRight).collect {
              case (item, Right(AddTorrentResponse(AddTorrentResult.Success(_, id, _), _, _))) =>
                StoredTorrent(0, item.showName, item.season, item.episode, item.link, id)
            }.map(store.storeTorrent))
          } yield storeResults
          Await.result(result, 5 minutes)
        }
      })

      StdIn.readLine()
      wsClient.close()
      system.terminate()
    case None =>
  }

}
