package com.liorregev.rssdownloader.store

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

final case class StoredTorrent(id: Int, showName: String, season: Int, episode: Int, link: String, torrentId: Int)

class Torrent(tag: Tag) extends Table[StoredTorrent](tag, "torrents") {
  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def showName: Rep[String] = column[String]("showName")
  def season: Rep[Int] = column[Int]("season")
  def episode: Rep[Int] = column[Int]("episode")
  def link: Rep[String] = column[String]("link")
  def torrentId: Rep[Int] = column[Int]("torrentId")

  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[StoredTorrent] = (id, showName, season, episode, link, torrentId) <> ((StoredTorrent.apply _).tupled, StoredTorrent.unapply)
}

class MySQLDownloadsStore {
  private val db = Database.forURL("jdbc:mysql://liornas:3306/torrents", "admin", "admin")
  private val torrents = TableQuery[Torrent]

  def storeTorrent(storedTorrent: StoredTorrent): Unit = {
    db.run(DBIO.seq(torrents += storedTorrent))
  }
}
