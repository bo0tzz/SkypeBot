package io.mazenmc.skypebot.stat

data class Message(private var contents: String, var time: Long) {
    fun contents(): String {
        return contents
    }

    fun time(): Long {
        return time
    }
}
