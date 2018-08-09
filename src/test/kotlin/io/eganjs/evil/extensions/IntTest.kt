package io.eganjs.evil.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IntTest {

    @Test
    fun `when call KB on Int then returned value is 4 KB`() {
        val expected = 4L * 1024

        val actual = 4.KB

        assertThat(actual)
                .`as`("4 KB")
                .isEqualTo(expected)
    }

    @Test
    fun `when call MB on Int then returned value is 3 MB`() {
        val expected = 3L * 1024 * 1024

        val actual = 3.MB

        assertThat(actual)
                .`as`("3 MB")
                .isEqualTo(expected)
    }

    @Test
    fun `when call GB on Int then returned value is 2 GB`() {
        val expected = 2L * 1024 * 1024 * 1024

        val actual = 2.GB

        assertThat(actual)
                .`as`("2 GB")
                .isEqualTo(expected)
    }
}
