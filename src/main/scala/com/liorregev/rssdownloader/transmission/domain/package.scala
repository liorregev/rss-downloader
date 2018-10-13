package com.liorregev.rssdownloader.transmission

import play.api.libs.json._

package object domain {
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

  implicit def requestWrites[T <: Request[_, _]]: OWrites[T] =
    (o: T) => JsObject(
      Map(
        "arguments" -> o.jsArgs,
        "tag" -> o.tag.map(i => JsNumber(i)).getOrElse(JsNull),
        "method" -> JsString(o.method)
      )
        .filterNot(_._2 == JsNull)
    )
}
