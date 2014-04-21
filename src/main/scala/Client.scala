import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.Service
import com.twitter.finagle.http.Http
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http._
import java.net.InetSocketAddress
import com.twitter.io.Charsets

object Client {

  def main(args: Array[String]) {
    val client: Service[HttpRequest, HttpResponse] = ClientBuilder()
      .codec(Http())
      .hosts(new InetSocketAddress(9090))
      .hostConnectionLimit(1)
      .build()

    val request1 = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
    request1.headers().add("X-Request-ID", "12345")
    val request2 = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")

    val response1: Future[HttpResponse] = client(request1)
    val response2: Future[HttpResponse] = client(request2)

    response1 onSuccess { resp =>
      val responseString = resp.getContent.toString(Charsets.Utf8)
      println("Response from server: " + responseString)
    }

    response2 onSuccess { resp =>
      val responseString = resp.getContent.toString(Charsets.Utf8)
      println("Response from server: " + responseString)
    }

    (response1 join response2) ensure {
      client.close()
    }
  }

}