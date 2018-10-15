package com.liorregev.rssdownloader.transmission.domain

import play.api.libs.json._

trait RequestType
trait Response[T <: RequestType] extends Product with Serializable {
  def result: String
  def tag: Option[Int]
}

trait Request[T <: RequestType, Resp <: Response[T]] extends Product with Serializable {
  def method: String
  def tag: Option[Int]
  def jsArgs: JsObject
  def responseReads: Reads[Resp]
}

