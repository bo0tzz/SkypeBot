package io.mazenmc.skypebot.modules

import com.samczsun.skype4j.chat.messages.ReceivedMessage
import io.mazenmc.skypebot.engine.bot.Command
import io.mazenmc.skypebot.engine.bot.Module
import io.mazenmc.skypebot.engine.bot.Optional
import io.mazenmc.skypebot.stat.MessageStatistic
import io.mazenmc.skypebot.stat.StatisticsManager
import io.mazenmc.skypebot.utils.Resource
import io.mazenmc.skypebot.utils.Utils
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom

object StatisticsModule: Module {
    private val format = DecimalFormat("##.#")

    @JvmStatic
    @Command(name = "whatwouldrandomquestion")
    fun cmdQuestion(chat: ReceivedMessage) {
        var statMap = StatisticsManager.statistics()
        var statistics = statMap.values.toMutableList()
        var stat = statistics[ThreadLocalRandom.current().nextInt(statistics.size)]
        var msg = stat.randomMessage()
        var message = msg.contents()

        while (message.startsWith("@") || (!message.endsWith("?") && !message.toLowerCase().startsWith("why"))) {
            stat = statistics[ThreadLocalRandom.current().nextInt(statistics.size)]
            msg = stat.randomMessage()
            message = msg.contents()
        }

        Resource.sendMessage(chat, "${stat.name} said \"$message\" at ${Date(msg.time).toString()}")
    }

    @JvmStatic
    @Command(name = "whatwouldrandomsay")
    fun cmdRandomSay(chat: ReceivedMessage) {
        var statMap = StatisticsManager.statistics()
        var username = statMap.keys.toList()[ThreadLocalRandom.current().nextInt(statMap.size)]
        randomSay(chat, username)
    }

    @JvmStatic
    @Command(name = "whatwouldselectsay")
    fun cmdSelectSay(chat: ReceivedMessage, username: String) {
        var stat = StatisticsManager.statistics()[username]

        if (stat == null) {
            Resource.sendMessage(chat, "No found statistic for $username!")
            return
        }

        randomSay(chat, username)
    }

    @JvmStatic
    @Command(name = "stats")
    fun cmdStats(chat: ReceivedMessage, @Optional person: String?) {
        if (person != null) {
            personStat(person, chat)
            return;
        }

        var toSend = ArrayList<String>()
        var messages = ArrayList<MessageStatistic>(StatisticsManager
                .statistics().values)

        Collections.sort(messages, {a, b -> b.letterCount() - a.letterCount()})

        var letterTotal = messages.map { m -> m.letterCount() }.sum()
        var total = messages.map { m -> m.messageAmount() }.sum()

        toSend.add("---------------------------------------")

        for (i in 0..5) {
            if (messages.size <= i) return

            var stat = messages[i]
            var percentage = (stat.letterCount().toDouble() / letterTotal) * 100

            toSend.add("${StatisticsManager.ownerFor(stat)}: ${stat.letterCount()} - " +
                    "${format.format(percentage)}%")
        }

        toSend.add("---------------------------------------")
        toSend.add("$letterTotal total characters sent in this chat")
        toSend.add("$total messages sent in this chat")
        toSend.add("${messages.size} members sent messages")

        var raw = messages.map { m -> m.messages().map { m -> m.contents() }.toMutableList() }
                .toMutableList()
        var msgs = ArrayList<String>(total)

        raw.forEach { i -> msgs.addAll(i) }

        var commands = msgs.filter { s -> s.startsWith("@") }
                .count()
        var characters = msgs.map { s -> s.length.toLong() }.sum()
        var words = ArrayList<String>()

        msgs.map { s -> s.split(" ") }
                .forEach { i -> words.addAll(i) }

        words.removeIf {i -> "".equals(i) || " ".equals(i)}

        var mostCommonWord = Utils.firstByFrequency(words)

        toSend.add("${(commands / total) * 100}% of those messages were commands")
        toSend.add("${words.size} words were sent")
        toSend.add("$characters characters were sent")
        toSend.add("---------------------------------------")
        toSend.add("Most common word: ${mostCommonWord.key} with ${mostCommonWord.value} occurences")

        Resource.sendMessages(toSend)
    }

    fun personStat(name: String, chat: ReceivedMessage) {
        var stat = StatisticsManager.statistics()[name]

        if (stat == null) {
            Resource.sendMessage(chat, "No found statistic for $name!")
            return;
        }

        var user = Utils.getUser(name)
        var name = Utils.getDisplayName(user)
        var toSend = ArrayList<String>(13)
        var first = Utils.firstSpoken(stat)
        var last = Utils.lastSpoken(stat)

        toSend.add("------ $name's statistics ------")
        toSend.add("Message count: ${stat.messageAmount()}")
        toSend.add("Word count: ${stat.wordCount()}")
        toSend.add("Average words per message: ${format.format(stat.averageWords())}")
        toSend.add("Command count: ${stat.commandCount()}")
        toSend.add("Random message: ${stat.randomMessage().contents()}")
        toSend.add("First message sent at ${Date(first.time).toString()}")
        toSend.add("First message: ${first.contents()}")
        toSend.add("Last message sent at ${Date(last.time).toString()}")
        toSend.add("Last message: ${last.contents()}")
        toSend.add("Percentage of messages which were commands: " +
                format.format(stat.commandPercent()))
        toSend.add("---------------------------------------")

        Resource.sendMessages(toSend)
    }

    fun randomSay(chat: ReceivedMessage, username: String) {
        var messageStats = StatisticsManager.statistics()
        var statistic = messageStats[username]
        var message = statistic!!.randomMessage()

        while (message.contents().startsWith("@") || message.contents().split(" ").size <= 3) {
            message = statistic.randomMessage()
        }

        if (message.contents().equals("<never sent message>")) {
            return
        }

        Resource.sendMessage(chat, "$username says ${message.contents()} at ${Date(message.time).toString()}")
    }
}