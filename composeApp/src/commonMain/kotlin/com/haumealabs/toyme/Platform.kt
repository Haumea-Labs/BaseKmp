package com.haumealabs.toyme

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform