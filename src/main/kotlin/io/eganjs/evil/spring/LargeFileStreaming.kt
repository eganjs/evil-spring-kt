package io.eganjs.evil.spring

import io.eganjs.evil.extensions.exhaust
import io.eganjs.evil.spring.config.ServerPortWatcher
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.InputStream
import java.io.OutputStream

@RestController
@RequestMapping("large-file")
class LargeFileController(
    restTemplateBuilder: RestTemplateBuilder,
    serverPortWatcher: ServerPortWatcher
) {

    val restTemplate: RestTemplate by lazy {
        serverPortWatcher.port
                .map { port ->
                    restTemplateBuilder
                            .rootUri("http://localhost:$port/large-file")
                            .requestFactory {
                                SimpleClientHttpRequestFactory().apply {
                                    setBufferRequestBody(false)
                                }
                            }
                            .build()
                }
                .blockingGet()
    }

    @GetMapping("ping")
    fun ping(): String = "pong"

    /**
     * Requires `spring.mvc.async.request-timeout` property to be set to a high value
     */
    @GetMapping
    fun download(@RequestParam("fileSize") fileSize: String): ResponseEntity<StreamingResponseBody> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.TEXT_PLAIN
            setContentDispositionFormData("attachment", "file.txt")
        }
        return ResponseEntity(createFileGenerator(fileSize.toLong()), headers, HttpStatus.OK)
    }

    /**
     * Requires using `RestTemplate.execute` to avoid loading response into memory
     */
    @GetMapping("virtual")
    fun virtualDownload(@RequestParam("fileSize") fileSize: Long): Long =
            downloadFile(fileSize) { byteStream -> byteStream.exhaust() }

    @PostMapping
    fun upload(file: InputStream): Long =
            file.exhaust()

    @PostMapping("virtual")
    fun virtualUpload(byteStream: InputStream): Long =
            uploadFile(byteStream)

    /**
     * Requires `SimpleClientHttpRequestFactory.bufferRequestBody` to be false to avoid loading request into memory
     */
    @GetMapping("transfer")
    fun transfer(@RequestParam("fileSize") fileSize: Long): Long =
            downloadFile(fileSize) { byteStream -> uploadFile(byteStream) }

    private inline fun <T> downloadFile(
        fileSize: Long,
        crossinline responseBodyHandler: (InputStream) -> T
    ): T {
        return restTemplate.execute(
                "/?fileSize={fileSize}",
                HttpMethod.GET,
                null,
                ResponseExtractor { response ->
                    responseBodyHandler(response.body)
                },
                fileSize
        )!!
    }

    private fun uploadFile(byteStream: InputStream): Long {
        return restTemplate.postForEntity(
                "/",
                InputStreamResource(byteStream),
                Long::class.java
        ).body!!
    }

    companion object {

        private val loremIpsum: String = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget leo fermentum, finibus nisi eu, luctus purus. Etiam mollis commodo varius. Morbi scelerisque elit non massa bibendum, at tempus neque fermentum. Quisque ultrices, lorem a luctus dictum, nibh nisi tincidunt velit, eget porta urna sem sed magna. Morbi viverra ante a imperdiet tincidunt. Etiam et nunc lacus. Nam diam lacus, condimentum vitae volutpat ut, maximus vel risus. Curabitur ac ultrices ligula.

            Fusce sit amet orci et magna venenatis mollis sit amet et leo. Donec eget ligula tortor. Cras suscipit aliquet mi sit amet mattis. Nullam id turpis id tellus sodales facilisis vitae a nulla. Nunc elementum odio tellus, a suscipit augue sodales in. Suspendisse facilisis tellus enim, eu viverra risus commodo et. Donec at est commodo, consectetur lorem eu, bibendum massa.

            Fusce arcu ante, elementum eget eros non, placerat finibus enim. Cras odio dui, pellentesque ac massa id, porta dignissim ligula. Nullam eu blandit velit, ac lacinia eros. Sed purus lacus, sollicitudin a congue quis, aliquam sed lectus. Nullam at tempor tortor, in gravida lectus. Phasellus et maximus nisl. Fusce eleifend id mi in venenatis. Pellentesque rutrum placerat molestie. Aliquam dictum, purus in dictum varius, enim eros fermentum lorem, sed laoreet risus elit sed lorem. Aliquam consectetur dictum ipsum, id volutpat enim accumsan sed. Donec eu nisi dolor. Duis in odio quis libero placerat luctus. In hac habitasse platea dictumst. Proin vestibulum a velit eget maximus. Cras pharetra est ut metus ullamcorper aliquet. Etiam placerat urna sed sem cursus dignissim.

            Donec iaculis gravida rutrum. Curabitur sit amet magna eleifend, tempus nunc non, fringilla tellus. Proin consequat maximus maximus. Nullam placerat tortor nec luctus pulvinar. Aliquam eget tempus erat. Integer vel eros faucibus, consequat ex vitae, dictum felis. Ut lobortis nunc at felis condimentum tristique sed a ipsum. Cras vitae pharetra nunc. Morbi blandit metus a diam sodales, id finibus eros faucibus. Morbi vel mi sed justo laoreet lacinia blandit in lorem.

            Phasellus ut facilisis sem. Integer elit sem, euismod nec congue at, iaculis non nunc. Sed egestas pharetra luctus. Nunc diam est, sagittis non feugiat sit amet, faucibus sit amet ante. In sit amet lectus et ipsum aliquet ultrices. Curabitur mollis tempus orci quis euismod. Etiam ex felis, luctus gravida consectetur ultrices, vehicula sed augue.
            """.trimIndent()

        private val loremIpsumBytes = loremIpsum.toByteArray()

        private fun createFileGenerator(fileSize: Long, source: ByteArray = loremIpsumBytes) =
                StreamingResponseBody { outputStream: OutputStream ->
                    (0 until fileSize)
                            .asSequence()
                            .map { it % source.size }
                            .map(Long::toInt)
                            .map(source::get)
                            .map(Byte::toInt)
                            .forEach(outputStream::write)
                }
    }
}
