package io.mazenmc.skypebot.stat

import com.samczsun.skype4j.chat.messages.ReceivedMessage
import org.json.JSONArray
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors
import java.util.stream.IntStream

class MessageStatistic(var name: String) {
    private var messages: MutableList<Message> = ArrayList()

    constructor(name: String, messages: JSONArray) : this(name) {
        IntStream.range(0, messages.length()).forEach { i ->
            var obj = messages.getJSONObject(i)
            var contents = obj.getString("contents")

            if (!contents.equals("<never sent message>")) {
                this.messages.add(Message(contents, obj.getLong("time")))
            }
        }

        if (this.messages.isEmpty()) {
            this.messages.add(Message("<never sent message>", System.currentTimeMillis()))
        }
    }

    fun name(): String {
        return name
    }

    fun words(): List<String> {
        var words = ArrayList<String>()

        messages.map { s -> s.contents().split(" ") }.forEach { s -> s.forEach { w -> words.add(w) } }

        return words
    }

    fun wordCount(): Int {
        return messages.map { m -> m.contents().split(" ").size }
                .filter { i -> i > 2 }
                .sum()
    }
    
    fun letterCount(): Int {
        return messages.map { m -> m.contents().toCharArray().filter { i -> i != ' ' }.size }
                .sum()
    }
    
    fun messageAmount(): Int {
        return messages.size
    }
    
    fun averageWords(): Int {
        return wordCount() / messageAmount()
    }
    
    fun randomMessage(): Message {
        return messages[ThreadLocalRandom.current().nextInt(messageAmount())]
    }
    
    fun commandCount(): Int {
        return messages.map(Message::contents)
                .filter { s -> s.startsWith("@") }
                .size
    }

    fun commandPercent(): Double {
        return (commandCount().toDouble() / messageAmount().toDouble()) * 100
    }

    fun addMessage(message: ReceivedMessage) {
        addMessage(Message(message.content.asPlaintext(), System.currentTimeMillis()))
    }

    fun addMessage(message: Message) {
        messages.add(message)
    }

    fun messages(): List<Message> {
        return messages
    }
}