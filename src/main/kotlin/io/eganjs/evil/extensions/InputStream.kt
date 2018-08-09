package io.eganjs.evil.extensions

import java.io.InputStream

fun InputStream.exhaust(): Long {
    var b = read()
    var i = 0L
    while (b != -1) {
        i++
        b = read()
    }
    return i
}
