package com.liorregev.rssdownloader

import java.nio.file.Files

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import ch.qos.logback.classic.util.ContextInitializer
import ch.qos.logback.classic.{Logger, LoggerContext}
import com.liorregev.rssdownloader.store.{MySQLDownloadsStore, StoredTorrent}
import com.liorregev.rssdownloader.transmission.Client
import com.liorregev.rssdownloader.transmission.domain.{TorrentActionRequest, TorrentActionType, TorrentIdentifiers}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.reflect.io.{Directory, Path}

object CompletionScript extends App {
  private val ALLOWED_EXTENSIONS = Seq(".mkv", ".mp4")

  final case class Config(torrentId: Int = 0, torrentDirectory: Directory = Path(".").toDirectory,
                          tvShowsDirectory: Directory = Path(".").toDirectory, serverIP: String = "")

  private val parser = new scopt.OptionParser[Config]("scopt") {
    head("scopt", "3.x")

    opt[Int]('i', "torrentId")
      .required()
      .action( (x, c) => c.copy(torrentId = x) )
      .text("The torrent ID")

    opt[String]('d', "torrentDirectory")
      .required()
      .action( (x, c) => c.copy(torrentDirectory = Path(x.trim).toDirectory))
      .text("The torrent directory")

    opt[String]('o', "tvShowsDirectory")
      .required()
      .action( (x, c) => c.copy(tvShowsDirectory = Path(x.trim).toDirectory) )
      .text("The directory for storing TV shows")

    opt[String]('s', "serverIP")
      .required()
      .action( (x, c) => c.copy(serverIP = x) )
      .text("The IP for the MySQL db and Transmission")
  }

  private def getFileExtension(file: Path): String = {
    val lastIndexOf = file.name.lastIndexOf(".")
    if (lastIndexOf == -1) {
      ""
    } else {
      file.name.substring(lastIndexOf)
    }
  }

  private def handleCompletion(config: Config): Unit = {
    implicit lazy val loggerFactory: LoggerContext = {
      val loggerContext = new LoggerContext()
      val contextInitializer = new ContextInitializer(loggerContext)
      contextInitializer.autoConfig()
      loggerContext
    }
    lazy val logger: Logger = loggerFactory.getLogger("com.liorregev.rssdownloader.CompletionScript")
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()
    try {
      val store = new MySQLDownloadsStore(s"jdbc:mysql://${config.serverIP}:3306/torrents")
      val txClient = new Client(s"http://${config.serverIP}:9092/transmission/rpc/")
      val eventualTorrentData: Future[Option[StoredTorrent]] = for {
        data <- store.loadByTorrentId(config.torrentId)
        _ <- txClient.request(TorrentActionRequest(TorrentActionType.Stop, TorrentIdentifiers.Id(config.torrentId)))
      } yield data
      val res = eventualTorrentData
        .collect {
          case Some(d) => d
        }
        .map {
          torrentData =>
            config
              .torrentDirectory
              .list
              .find(f => ALLOWED_EXTENSIONS.contains(getFileExtension(f)))
              .foreach {
                file =>
                  val seriesSeasonPath = config.tvShowsDirectory / torrentData.showName / s"Season ${torrentData.season}"
                  seriesSeasonPath.createDirectory()
                  val newFileName = s"${torrentData.showName.replaceAll(raw"[^\w\d]", "").toLowerCase}.s${torrentData.season}.e${"%02d".format(torrentData.episode)}${getFileExtension(file)}"
                  val newFilePath = seriesSeasonPath / newFileName
                  logger.info(s"Moving ${file.path} -> ${newFilePath.path}")
                  Files.move(file.jfile.toPath, newFilePath.jfile.toPath)
              }
        }
      Await.result(res, 3 minutes)
    } finally {
      wsClient.close()
      system.terminate()
    }
  }

  // parser.parse returns Option[C]
  parser.parse(args, Config()) match {
    case Some(config) => handleCompletion(config)
    case None =>
  }
}
