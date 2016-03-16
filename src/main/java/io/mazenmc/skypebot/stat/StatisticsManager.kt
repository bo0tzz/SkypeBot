package io.mazenmc.skypebot.stat

import com.samczsun.skype4j.chat.messages.ReceivedMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

object StatisticsManager {
    private val statistics: MutableMap<String, MessageStatistic> = ConcurrentHashMap<String, MessageStatistic>()

    fun logMessage(message: ReceivedMessage) {
        var senderId = message.sender.username

        if (!statistics.containsKey(senderId))
            statistics.put(senderId, MessageStatistic(senderId))

        statistics.get(senderId)!!.addMessage(message)
    }

    fun removeStat(id: String) {
        statistics.remove(id)
    }

    fun ownerFor(statistic: MessageStatistic): String {
        return statistics.filter { e -> e.value.equals(statistic) }.iterator().next().key
    }

    fun statistics(): Map<String, MessageStatistic> {
        return statistics
    }

    fun loadStatistics() {
        var source = StringBuilder()

        try {
            for (s in Files.readAllLines(Paths.get("statistics.json"))) {
                source.append(s).append("\n")
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        try {
            var obj = JSONObject(source.toString())
            var keys = obj.keys()

            while (keys.hasNext()) {
                var ob = keys.next()

                if (ob !is String)
                    continue

                statistics.put(ob, MessageStatistic(ob, obj.getJSONArray(ob)))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun saveStatistics() {
        var json = JSONObject()

        statistics.entries.forEach { e ->
            var array = JSONArray()

            e.value.messages().forEach { m ->
                var obj = JSONObject()

                obj.put("contents", m.contents())
                obj.put("time", m.time)

                array.put(obj)
            }

            json.put(e.key, array)
        }

        Files.write(Paths.get("statistics.json"), json.toString().toByteArray(Charset.defaultCharset()))
    }
}