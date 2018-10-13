package com.liorregev.rssdownloader

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.liorregev.rssdownloader.reader.RssReader
import com.liorregev.rssdownloader.store.{MySQLDownloadsStore, StoredTorrent}
import com.liorregev.rssdownloader.transmission.Client
import com.liorregev.rssdownloader.transmission.domain.{AddTorrentRequest, AddTorrentResponse, AddTorrentResult, TorrentSource}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object Main extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

  val rssReader = new RssReader("http://showrss.info/user/112814.rss?magnets=true&namespaces=true&name=clean&quality=hd&re=null")
  val txClient = new Client("http://liornas:9092/transmission/rpc/")
  val store = new MySQLDownloadsStore("jdbc:mysql://liornas:3306/torrents")

  system.scheduler.schedule(Duration.Zero, 10 minutes, new Runnable {
    override def run(): Unit = {
      val result = for {
        items <- rssReader.read()
        queriedItems <- Future.sequence(items.map(item => store.containsItem(item).map(item -> _)))
        resps <- Future.sequence(queriedItems.filterNot(_._2).map(_._1)
          .map(item => txClient.request(AddTorrentRequest(TorrentSource.Filename(item.link))).map(item -> _)))
        storeResults <- Future.sequence(resps.filter(_._2.isRight).collect {
          case (item, Right(AddTorrentResponse(AddTorrentResult.Success(_, id, _), _, _))) =>
            StoredTorrent(0, item.showName, item.season, item.episode, item.link, id)
        }.map(store.storeTorrent))
      } yield storeResults
      Await.result(result, 5 minutes)
    }
  })
}
