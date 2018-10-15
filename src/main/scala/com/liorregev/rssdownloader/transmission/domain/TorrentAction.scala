package com.liorregev.rssdownloader.transmission.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._

case object TorrentAction extends RequestType

final case class TorrentActionResponse(result: String, tag: Option[Int])
  extends Response[TorrentAction.type]

sealed trait TorrentActionType {
  def method: String
}
object TorrentActionType {
  case object Start extends TorrentActionType {
    override val method: String = "torrent-start"
  }
  case object StartNow extends TorrentActionType {
    override val method: String = "torrent-start-now"
  }
  case object Stop extends TorrentActionType {
    override val method: String = "torrent-stop"
  }
  case object Verify extends TorrentActionType {
    override val method: String = "torrent-verify"
  }
  case object Reannounce extends TorrentActionType {
    override val method: String = "torrent-reannounce"
  }
}

sealed trait TorrentIdentifiers {
  def toJsValue: JsValue
}
object TorrentIdentifiers {
  final case class Ids(hashIds: Seq[String] = Nil, numericIds: Seq[Int] = Nil) extends TorrentIdentifiers {
    override def toJsValue: JsValue = JsArray(hashIds.map(JsString)) ++ JsArray(numericIds.map(i => JsNumber(i)))
  }
  final case class Id(id: Int) extends TorrentIdentifiers {
    override def toJsValue: JsValue = JsNumber(id)
  }
  case object RecentlyActive extends TorrentIdentifiers {
    override def toJsValue: JsValue = JsString("recently-active")
  }
}

final case class TorrentActionRequest(action: TorrentActionType, identifiers: TorrentIdentifiers,
                                      tag: Option[Int] = None)
  extends Request[TorrentAction.type, TorrentActionResponse] {

  override val method: String = action.method
  override val jsArgs: JsObject = {
    Json.obj(
      "ids" -> identifiers.toJsValue
    )
  }

  def responseReads: Reads[TorrentActionResponse] = (
      (__ \ "result").read[String] and
      (__ \ "tag").readNullable[Int]
    )(TorrentActionResponse.apply _)
}
