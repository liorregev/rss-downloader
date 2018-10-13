package com.liorregev.rssdownloader.reader

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import javax.xml.parsers.DocumentBuilderFactory
import play.api.libs.ws._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

final case class Item(title: String, link: String, showName: String, season: Int, episode: Int)

object Item {
  def fromEntry(entry: SyndEntry): Option[Item] = {
    val markups = entry
      .getForeignMarkup
      .asScala
      .map {
        elem => elem.getName -> elem.getContent(0).getValue
      }.toMap
    for {
      showName <- markups.get("show_name")
      (season, episode) <- {
        val titleRegex = raw"${Pattern.quote(markups.getOrElse("show_name", ""))} (\d+)x(\d+) .* 720p".r
        entry.getTitle match {
          case titleRegex(season, episode) =>
            Option((season, episode))
          case _ => None
        }
      }
    } yield Item(entry.getTitle, entry.getLink, showName, season.toInt, episode.toInt)
  }
}

class RssReader(rssUrl: String)(implicit wsClient: StandaloneWSClient) {
  def read()
          (implicit ec: ExecutionContext): Future[List[Item]] = {
    val input = new SyndFeedInput()
    wsClient
      .url(rssUrl)
      .get()
      .map(_.body)
      .map(body => new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)))
      .map(body => {
        val dbFactory = DocumentBuilderFactory.newInstance
        val dBuilder = dbFactory.newDocumentBuilder
        dBuilder.parse(body)
      })
      .map(input.build)
      .map(_.getEntries.asScala.toList)
      .map(_.flatMap(Item.fromEntry))
  }
}
