package com.liorregev.rssdownloader.store

import com.liorregev.rssdownloader.reader.Item
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.SqlType

import scala.concurrent.Future

final case class StoredTorrent(id: Int, showName: String, season: Int, episode: Int, link: String, torrentId: Int)

class Torrent(tag: Tag) extends Table[StoredTorrent](tag, "torrents") {
  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def showName: Rep[String] = column[String]("showName")
  def season: Rep[Int] = column[Int]("season")
  def episode: Rep[Int] = column[Int]("episode")
  def link: Rep[String] = column[String]("link", SqlType("TEXT"))
  def torrentId: Rep[Int] = column[Int]("torrentId")

  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[StoredTorrent] = (id, showName, season, episode, link, torrentId) <> ((StoredTorrent.apply _).tupled, StoredTorrent.unapply)
}

class MySQLDownloadsStore(dbURL: String) {
  private val db = Database.forURL(dbURL, "admin", "admin")
  private val torrents = TableQuery[Torrent]

  def storeTorrent(storedTorrent: StoredTorrent): Future[Unit] = {
    println(s"SQL - Adding $storedTorrent")
    db.run(DBIO.seq(torrents += storedTorrent))
  }

  def containsItem(item: Item): Future[Boolean] = {
    println(s"SQL - Checking $item")
    val query = torrents
      .filter(torrent => torrent.showName === item.showName)
      .filter(torrent => torrent.season === item.season)
      .filter(torrent => torrent.episode === item.episode)
      .exists
      .result
    db.run(query)
  }
}
