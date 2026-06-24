package ch.yoinc.recall

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform