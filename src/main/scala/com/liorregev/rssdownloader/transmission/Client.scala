package com.liorregev.rssdownloader.transmission

import com.liorregev.rssdownloader.transmission.domain.{Request, RequestType, Response}
import play.api.libs.json._
import play.api.libs.ws.{JsonBodyReadables, JsonBodyWritables, StandaloneWSClient}

import scala.concurrent.{ExecutionContext, Future}
import cats.syntax.either._
import ch.qos.logback.classic.LoggerContext

sealed trait ClientError
final case class ResponseParseError(jsError: JsError) extends ClientError

class Client(url: String)(implicit wsClient: StandaloneWSClient, loggerFactory: LoggerContext)
  extends JsonBodyWritables with JsonBodyReadables {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var csrfToken: Option[String] = None

  private lazy val logger = loggerFactory.getLogger(getClass)

  def request[T <: RequestType, Resp <: Response[T]](req: Request[T, Resp])
                                                    (implicit writes: Writes[Request[T, Resp]],
                                                     ec: ExecutionContext): Future[Either[ClientError, Resp]] = {
    logger.info(s"Processing $req")
    runRequest(req)
      .map(req.responseReads.reads)
      .map {
        case JsSuccess(value, _) => value.asRight[ClientError]
        case err: JsError => ResponseParseError(err).asLeft[Resp]
      }
  }

  private def runRequest[Resp <: Response[T], T <: RequestType](req: Request[T, Resp])
                                                               (implicit writes: Writes[Request[T, Resp]], ec: ExecutionContext): Future[JsValue] = {
    logger.debug(s"Sending request $req")
    wsClient.url(url)
      .addHttpHeaders(csrfToken.map(Client.CSRF_HEADER -> _).toSeq: _*)
      .post(Json.toJson(req))
      .flatMap(resp => {
        if (resp.status == 409) {
          logger.warn(s"Got 409, setting CSRF and re-sending")
          csrfToken = resp.header(Client.CSRF_HEADER)
          runRequest(req)
        } else {
          Future(resp.body[JsValue])
        }
      })
  }
}

object Client {
  val CSRF_HEADER = "X-Transmission-Session-Id"
}
