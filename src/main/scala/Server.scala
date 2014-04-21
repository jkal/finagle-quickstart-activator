import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.{Http, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import java.net.InetSocketAddress
import org.jboss.netty.handler.codec.http._
import scala.util.Random

object Server {

  val filter = new SimpleFilter[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      // Check if a request header exists and add it if it doesn't.
      val id = request.getHeader("X-Request-ID")
      if (id == null) {
        request.setHeader("X-Request-ID", Random.alphanumeric.take(5).mkString)
      }
      service(request)
    }
  }

  val response = new Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest) = {
      val id = request.getHeader("X-Request-ID")
      val response = Response()
      response.setContentString(s"Hello from Finagle! (ID: $id)\n")
      Future.value(response)
    }
  }

  def main(args: Array[String]) {
    println("Start HTTP server on port 9090")

    val service = filter andThen response

    val server = ServerBuilder()
      .codec(Http())
      .bindTo(new InetSocketAddress(9090))
      .name("httpserver")
      .build(service)
  }

}