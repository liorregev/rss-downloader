package com.liorregev.rssdownloader.transmission.domain

import play.api.libs.json._
import play.api.libs.functional.syntax._
import shapeless.syntax.std.tuple._

case object AddTorrent extends RequestType

sealed trait AddTorrentResult {
  def hashString: String
  def id: Int
  def name: String
}

object AddTorrentResult {
  final case class Success(hashString: String, id: Int, name: String) extends AddTorrentResult
  final case class Duplicate(hashString: String, id: Int, name: String) extends AddTorrentResult
  case object Failure extends AddTorrentResult {
    override def hashString: String = ""
    override def id: Int = 0
    override def name: String = ""
  }

  private val successReads = Json.reads[Success]
  private val duplicateReads = Json.reads[Success]
  private val failureReads = Reads.pure(Failure)

  implicit val reads: Reads[AddTorrentResult] = {
    case JsObject(underlying) =>
      underlying.headOption match {
        case Some(("torrent-added", obj)) => successReads.reads(obj)
        case Some(("torrent-duplicate", obj)) => duplicateReads.reads(obj)
        case _ => failureReads.reads(JsNull)
      }
    case _ => JsError("Not a JsObject")
  }
}

final case class AddTorrentResponse(addResult: AddTorrentResult, result: String, tag: Option[Int])
  extends Response[AddTorrent.type]

sealed trait TorrentSource
object TorrentSource {
  final case class Metainfo(metainfo: String) extends TorrentSource
  final case class Filename(filename: String) extends TorrentSource
}

final case class AddTorrentRequest(torrentSource: TorrentSource, cookies: Option[String] = None,
                                   downloadDir: Option[String] = None, paused: Option[Boolean] = None,
                                   peerLimit: Option[Int] = None, bandwidthPriority: Option[Int] = None,
                                   filesWanted: Option[List[Int]] = None, filesUnwanted: Option[List[Int]] = None,
                                   priorityHigh: Option[List[Int]] = None, priorityLow: Option[List[Int]] = None,
                                   priorityNormal: Option[List[Int]] = None, tag: Option[Int] = None)
  extends Request[AddTorrent.type, AddTorrentResponse] {
  override val method: String = "torrent-add"
  override val jsArgs: JsObject = {
    val torrentInfoWrites: OWrites[TorrentSource] = {
      case TorrentSource.Metainfo(metainfo) => Json.obj(
        "metainfo" -> metainfo
      )
      case TorrentSource.Filename(filename) => Json.obj(
        "filename" -> filename
      )
    }
    val writes: OWrites[AddTorrentRequest] = (
      torrentInfoWrites and
        (JsPath \ "cookies").writeNullable[String] and
        (JsPath \ "download-dir").writeNullable[String] and
        (JsPath \ "paused").writeNullable[Boolean] and
        (JsPath \ "peer-limit").writeNullable[Int] and
        (JsPath \ "bandwidthPriority").writeNullable[Int] and
        (JsPath \ "files-wanted").writeNullable[List[Int]] and
        (JsPath \ "files-unwanted").writeNullable[List[Int]] and
        (JsPath \ "priority-high").writeNullable[List[Int]] and
        (JsPath \ "priority-low").writeNullable[List[Int]] and
        (JsPath \ "priority-normal").writeNullable[List[Int]]
      )(unlift((o: AddTorrentRequest) => AddTorrentRequest.unapply(o).map(_.init)))
    writes.writes(this)
  }

  def responseReads: Reads[AddTorrentResponse] = (
    (__ \ "arguments").read[AddTorrentResult] and
      (__ \ "result").read[String] and
      (__ \ "tag").readNullable[Int]
    )(AddTorrentResponse.apply _)
}
