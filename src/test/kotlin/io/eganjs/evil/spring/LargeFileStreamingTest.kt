package io.eganjs.evil.spring

import io.eganjs.evil.extensions.KB
import io.eganjs.evil.extensions.exhaust
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LargeFileStreamingTest {

    @LocalServerPort
    var port: Int = 8080

    private fun template() = RequestSpecBuilder()
            .setBaseUri("http://localhost:$port/large-file")
            .build()

    @Test
    fun `when send request to ping then pong is returned`() {
        val expected = "pong"

        val response = given(template())
                .get("ping")

        assertThat(response.statusCode)
                .`as`("response status code")
                .isEqualTo(200)

        val actual = response.asString()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when request download 1 KB file then file size is 1 KB`() {
        val expected = 1.KB

        val response = given(template())
                .param("fileSize", 1.KB)
                .get()

        assertThat(response.statusCode)
                .`as`("response status code")
                .isEqualTo(200)

        val actual = response.asInputStream().exhaust()

        assertThat(actual)
                .`as`("downloaded file size")
                .isEqualTo(expected)
    }

    @Test
    fun `when upload 2 KB file then uploaded file size is 2 KB`() {
        val expected = 2.KB

        val file = ByteArray(2.KB.toInt())
        val response = given(template())
                .body(file)
                .post()

        assertThat(response.statusCode)
                .`as`("response status code")
                .isEqualTo(200)

        val actual = response.body.`as`<Long>(Long::class.java)

        assertThat(actual)
                .`as`("uploaded file size")
                .isEqualTo(expected)
    }

    @Test
    fun `when virtual download 3 KB file then file size downloaded by server is 3 KB`() {
        val expected = 3.KB

        val response = given(template())
                .param("fileSize", 3.KB)
                .get("virtual")

        assertThat(response.statusCode)
                .`as`("response status code")
                .isEqualTo(200)

        val actual = response.body.`as`<Long>(Long::class.java)

        assertThat(actual)
                .`as`("file size downloaded by server")
                .isEqualTo(expected)
    }

    @Test
    fun `when virtual upload 4 KB file then file size uploaded by server is 4 KB`() {
        val expected = 4.KB

        val file = ByteArray(4.KB.toInt())
        val response = given(template())
                .body(file)
                .post("virtual")

        assertThat(response.statusCode)
                .`as`("response status code")
                .isEqualTo(200)

        val actual = response.body.`as`<Long>(Long::class.java)

        assertThat(actual)
                .`as`("uploaded file size")
                .isEqualTo(expected)
    }

    @Test
    fun `when request 5 KB file transfer then file size transferred by server is 5 KB`() {
        val expected = 5.KB

        val response = given(template())
                .param("fileSize", 5.KB)
                .get("transfer")

        assertThat(response.statusCode)
                .`as`("response status code")
                .isEqualTo(200)

        val actual = response.body.`as`<Long>(Long::class.java)

        assertThat(actual)
                .`as`("file size transferred by server")
                .isEqualTo(expected)
    }
}


