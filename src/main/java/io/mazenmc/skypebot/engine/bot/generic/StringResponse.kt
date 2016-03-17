package io.mazenmc.skypebot.engine.bot.generic

import com.samczsun.skype4j.chat.messages.ReceivedMessage
import io.mazenmc.skypebot.utils.Utils
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Predicate
import kotlin.properties.Delegates

open class StringResponse(val template: String) {
    val arrays: MutableMap<String, Array<String>> = HashMap()
    var predicate: Predicate<ReceivedMessage>? = null
    var lastArgs: MutableList<String> by Delegates.notNull() // let's just say I lost my mind

    /*
     * Take the string: "[sender.nick]'s first arg is [arg.1], their third was [arg.3], and their second was [arg.2]"
     * Let's give it the input "I like cats"
     *
     * The method will first replace [sender.nick] to the display name, which let's say is "Mazen"
     *
     * Then it will start to replace my args. It'll first look for [arg.
     * It finds the first instance at index 29, which is the [
     *
     * Beautiful! Now it'll find the position in which the template wishes to find the argument
     * The char describing this will be in index + 5 which is 34 in this case.
     * It fetches the integer value from the char in that position, and it's one in this case.
     *
     * It gathers the replacement text from the input, and replaces the full [arg.1] with the appropriate argument
     *
     * It will continuously repeat this process until there are no longer any "[arg."'s left
     *
     * After doing this process, the method returns a response like this:
     * "Mazen's first arg is I, their third was cats, and their second was like"
     *
     * https://ideone.com/QjLvMI
     */
    open fun process(message: ReceivedMessage): String {
        if (predicate != null && !predicate!!.test(message)) {
            return "N/A";
        }

        var response = template.replace("[sender.id]", message.sender.username)
                .replace("[sender.nick]", Utils.getDisplayName(message.sender))
        var args: MutableCollection<String> = message.content.asPlaintext().split(" ").toMutableSet()

        args.remove(args.first()) // remove @[command]
        args = args.toMutableList()

        if (args !is MutableList) {
            return "" // not going to happen, for casts only
        }

        lastArgs = args

        while (response.contains("[ar.")) {
            var index = response.indexOf("[ar.")
            var sub = response.substring(index + 3, response.length) // + 3 to get rid of starter info
            var subEnd = sub.indexOf(']')
            var repl = arrays[sub.substring(1, subEnd)] ?: arrayOf("Invalid array name provided")

            response = response.replace(response.substring(index, index + subEnd + 4),
                    repl[ThreadLocalRandom.current().nextInt(repl.size)])
        }

        while (response.contains("[arg.")) {
            var index = response.indexOf("[arg.")
            var ch = response[index + 5]

            if (ch == 'a') {
                response = response.replace(response.substring(index, index + 7), Utils.compiledArgs(message))
                continue
            }

            var argIndex = Integer.parseInt(ch.toString())
            var replace = args[argIndex]

            if ("".equals(replace))
                return "Insufficient arguments provided"

            if (checkSafe(replace)) // there's a smarter way to avoid this, but this is simple
                return "Yeahhh--no, I'm not going to allow you to enter ${replace}"

            response = response.replace(response.substring(index, index + 7), replace)
        }

        while (response.contains("[r.")) {
            var index = response.indexOf("[r.")
            var sub = response.substring(index + 3, response.length) // + 3 to get rid of starter info
            var subEnd = sub.indexOf(']')
            var bound = Integer.parseInt(sub.substring(0, subEnd))

            response = response.replace(response.substring(index, index + subEnd),
                    ThreadLocalRandom.current().nextInt(bound).toString())
        }

        return response
    }

    fun addArray(name: String, array: Array<String>): StringResponse {
        arrays.put(name, array)
        return this
    }

    fun predicate(predicate: Predicate<ReceivedMessage>): StringResponse {
        this.predicate = predicate;
        return this
    }

    fun checkSafe(input: String): Boolean {
        return input.contains("[arg.") || input.contains("[sender.id]") ||
                input.contains("[sender.nick]") || input.contains("[r.") ||
                input.contains("[ar.")
    }
}