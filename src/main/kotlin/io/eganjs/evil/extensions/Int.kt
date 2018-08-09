package io.eganjs.evil.extensions

inline val Int.GB: Long
    get() = 1024L * this.MB

inline val Int.MB: Long
    get() = 1024L * this.KB

inline val Int.KB: Long
    get() = 1024L * this
