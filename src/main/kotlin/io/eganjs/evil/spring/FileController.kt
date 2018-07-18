package io.eganjs.evil.spring

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.InputStream
import java.io.OutputStream

@RestController
@RequestMapping("large-file")
class FileController(restTemplateBuilder: RestTemplateBuilder) {

    private val restTemplate = restTemplateBuilder
            .rootUri("http://localhost:8080/large-file")
            .requestFactory {
                SimpleClientHttpRequestFactory().apply {
                    setBufferRequestBody(false)
                }
            }
            .build()

    /**
     * Requires `spring.mvc.async.request-timeout` property to be set to a high value
     */
    @GetMapping
    fun download(@RequestParam("bytes") bytes: String): ResponseEntity<StreamingResponseBody> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.TEXT_PLAIN
            setContentDispositionFormData("attachment", "file.txt")
        }
        return ResponseEntity(createFileGenerator(bytes.toInt()), headers, HttpStatus.OK)
    }

    /**
     * Requires using `RestTemplate.execute` to avoid loading response into memory
     */
    @GetMapping("virtual")
    fun virtualDownload(@RequestParam("bytes") bytes: String): Long {
        return restTemplate.execute(
                "/?bytes={bytes}",
                HttpMethod.GET,
                null,
                ResponseExtractor { response ->
                    response.body.exhaust()
                },
                bytes
        )!!
    }

    @PostMapping
    fun upload(inputStream: InputStream) = inputStream.exhaust()

    /**
     * Requires `SimpleClientHttpRequestFactory.bufferRequestBody` to be false to avoid loading request into memory
     */
    @GetMapping("passthrough")
    fun passthrough(@RequestParam("bytes") bytes: String): Long {
        return restTemplate.execute(
                "/?bytes={bytes}",
                HttpMethod.GET,
                null,
                ResponseExtractor { response ->
                    restTemplate.postForEntity(
                            "/",
                            InputStreamResource(response.body),
                            Long::class.java
                    ).body
                },
                bytes
        )!!
    }


    companion object {

        private val loremIpsum: String = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget leo fermentum, finibus nisi eu, luctus purus. Etiam mollis commodo varius. Morbi scelerisque elit non massa bibendum, at tempus neque fermentum. Quisque ultrices, lorem a luctus dictum, nibh nisi tincidunt velit, eget porta urna sem sed magna. Morbi viverra ante a imperdiet tincidunt. Etiam et nunc lacus. Nam diam lacus, condimentum vitae volutpat ut, maximus vel risus. Curabitur ac ultrices ligula.

            Fusce sit amet orci et magna venenatis mollis sit amet et leo. Donec eget ligula tortor. Cras suscipit aliquet mi sit amet mattis. Nullam id turpis id tellus sodales facilisis vitae a nulla. Nunc elementum odio tellus, a suscipit augue sodales in. Suspendisse facilisis tellus enim, eu viverra risus commodo et. Donec at est commodo, consectetur lorem eu, bibendum massa.

            Fusce arcu ante, elementum eget eros non, placerat finibus enim. Cras odio dui, pellentesque ac massa id, porta dignissim ligula. Nullam eu blandit velit, ac lacinia eros. Sed purus lacus, sollicitudin a congue quis, aliquam sed lectus. Nullam at tempor tortor, in gravida lectus. Phasellus et maximus nisl. Fusce eleifend id mi in venenatis. Pellentesque rutrum placerat molestie. Aliquam dictum, purus in dictum varius, enim eros fermentum lorem, sed laoreet risus elit sed lorem. Aliquam consectetur dictum ipsum, id volutpat enim accumsan sed. Donec eu nisi dolor. Duis in odio quis libero placerat luctus. In hac habitasse platea dictumst. Proin vestibulum a velit eget maximus. Cras pharetra est ut metus ullamcorper aliquet. Etiam placerat urna sed sem cursus dignissim.

            Donec iaculis gravida rutrum. Curabitur sit amet magna eleifend, tempus nunc non, fringilla tellus. Proin consequat maximus maximus. Nullam placerat tortor nec luctus pulvinar. Aliquam eget tempus erat. Integer vel eros faucibus, consequat ex vitae, dictum felis. Ut lobortis nunc at felis condimentum tristique sed a ipsum. Cras vitae pharetra nunc. Morbi blandit metus a diam sodales, id finibus eros faucibus. Morbi vel mi sed justo laoreet lacinia blandit in lorem.

            Phasellus ut facilisis sem. Integer elit sem, euismod nec congue at, iaculis non nunc. Sed egestas pharetra luctus. Nunc diam est, sagittis non feugiat sit amet, faucibus sit amet ante. In sit amet lectus et ipsum aliquet ultrices. Curabitur mollis tempus orci quis euismod. Etiam ex felis, luctus gravida consectetur ultrices, vehicula sed augue.
            """.trimIndent()

        private fun createFileGenerator(bytes: Int) = StreamingResponseBody { outputStream: OutputStream ->
            generateSequence { loremIpsum.toByteArray().asSequence() }
                    .flatMap { it }
                    .take(bytes)
                    .map(Byte::toInt)
                    .forEach(outputStream::write)
        }

        private fun InputStream.exhaust(): Long {
            var b = read()
            var i: Long = 0
            while (b != -1) {
                b = read()
                i++
            }
            return i
        }
    }
}

