package io.eganjs.evil.spring

import io.eganjs.evil.extensions.MB
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
    fun `when request download 751 MB file then file size is 751 MB`() {
        val expected = 751.MB

        val response = given(template())
                .param("fileSize", 751.MB)
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
    fun `when upload 752 MB file then uploaded file size is 752 MB`() {
        val expected = 752.MB

        val file = ByteArray(752.MB.toInt())
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
    fun `when virtual download 753 MB file then file size downloaded by server is 753 MB`() {
        val expected = 753.MB

        val response = given(template())
                .param("fileSize", 753.MB)
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
    fun `when virtual upload 754 MB file then file size uploaded by server is 754 MB`() {
        val expected = 754.MB

        val file = ByteArray(754.MB.toInt())
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
    fun `when request 755 MB file transfer then file size transferred by server is 755 MB`() {
        val expected = 755.MB

        val response = given(template())
                .param("fileSize", 755.MB)
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
