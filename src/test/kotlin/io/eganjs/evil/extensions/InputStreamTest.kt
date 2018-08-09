package io.eganjs.evil.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream

class InputStreamTest {

    @Test
    fun `when call exhause on InputStream then returned value is 1024`() {
        val expected = 1.KB

        val actual = ByteArrayInputStream(ByteArray(expected.toInt())).exhaust()

        assertThat(actual)
                .`as`("file size")
                .isEqualTo(expected)
    }
}
