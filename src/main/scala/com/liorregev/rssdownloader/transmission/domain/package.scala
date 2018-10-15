package com.liorregev.rssdownloader.transmission

import play.api.libs.json._

package object domain {
  implicit def requestWrites[T <: Request[_, _]]: OWrites[T] =
    (o: T) => JsObject(
      Map[String, JsValue](
        "arguments" -> o.jsArgs,
        "tag" -> o.tag.map(i => JsNumber(i): JsValue).getOrElse(JsNull),
        "method" -> JsString(o.method)
      )
        .filterNot(_._2 == JsNull)
    )
}
