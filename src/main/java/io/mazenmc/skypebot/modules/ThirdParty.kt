package io.mazenmc.skypebot.modules

import com.mashape.unirest.http.Unirest
import com.samczsun.skype4j.chat.messages.ReceivedMessage
import io.mazenmc.skypebot.SkypeBot
import io.mazenmc.skypebot.engine.bot.Command
import io.mazenmc.skypebot.engine.bot.Module
import io.mazenmc.skypebot.utils.CommandResources
import io.mazenmc.skypebot.utils.Resource
import java.net.URLEncoder

object ThirdParty : Module {
    @Command(name = "twitter\\.com\\/[A-z0-9]+\\/status\\/[0-9]{18}", command = false, exact = false)
    fun cmdTwitter(chat: ReceivedMessage) {
        var wholeMessage = chat.content.asPlaintext()
        var idMatcher = Resource.TWITTER_REGEX.matcher(wholeMessage)

        if (!idMatcher.find()) {
            return // invalid twitter url
        }

        var tweetId = idMatcher.group(2) ?: return
        var status = SkypeBot.twitter()!!.tweets().showStatus(tweetId.toLong())

        Resource.sendMessage(chat, "${status.user.name}: ${status.text}")
    }

    @Command(name = "en.wikipedia.org/wiki/", command = false, exact = false)
    fun cmdWikipedia(chat: ReceivedMessage) {
        var wholeMessage = chat.content.asPlaintext()
        var idMatcher = Resource.WIKIPEDIA_REGEX.matcher(wholeMessage)

        if (!idMatcher.find()) {
            return // invalid twitter url
        }

        var articleId = idMatcher.group(1).split(" ")[0]

        if ("".equals(articleId)) return

        var json = Unirest.get("http://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exsentences=10" +
                "&titles=${articleId}&redirects=true&continue=&explaintext=").asJson().getBody().getObject()
                .getJSONObject("query").getJSONObject("pages")
        var key = ""
        var keys = json.keys()

        while (keys.hasNext()) {
            key = keys.next() as String
        }

        var snippet = "Error finding wikipedia article"

        if ("-1".equals(key)) {
            snippet = "*${json.getJSONObject(key).getString("title")}*\n${json.getJSONObject(key)
                    .getString("extract").split("\n")[0]}"
        }

        Resource.sendMessage(chat, snippet)
    }

    @Command(name = "fuckingweather", alias = arrayOf("yellingweather"))
    fun cmdWeather(chat: ReceivedMessage, location: String) {
        var call = (CommandResources.weatherUrl + location).replace(' ', '+')
        var json = Unirest.get(call).asJson().body.`object`

        if (json.getInt("cod") != 200) {
            Resource.sendMessage(chat, "I CAN'T GET THE FUCKING WEATHER")
        }

        var temp = Math.round(json.getJSONObject("main").getDouble("temp"))
        var metric = Math.round((temp - 32) / 1.8000)

        if (temp <= 32) {
            Resource.sendMessage(chat, "ITS FUCKING FREEZING!");
        } else if (temp >= 33 && temp <= 60) {
            Resource.sendMessage(chat, "ITS FUCKING COLD!");
        } else if (temp >= 61 && temp <= 75) {
            Resource.sendMessage(chat, "ITS FUCKING NICE!");
        } else {
            Resource.sendMessage(chat, "ITS FUCKING HOT");
        }

        Resource.sendMessage(chat, "THE FUCKING WEATHER IN ${location.toUpperCase()} IS ${temp} F | ${metric} C")
    }

    @Command(name = "define")
    fun cmdDefine(chat: ReceivedMessage, word: String) {
        var json = Unirest.get("http://api.urbandictionary.com/v0/define?term=${URLEncoder.encode(word, "UTF-8")}")
                .asJson().body.`object`
        var status = json.getString("result_type")

        if ("no_results".equals(status)) {
            Resource.sendMessage(chat, "No results found for ${word}")
            return;
        }

        var definition = json.getJSONArray("list").getJSONObject(0)
        var author = definition.getString("author")
        var ups = definition.getInt("thumbs_up")
        var downs = definition.getInt("thumbs_down")
        var def = definition.getString("definition")
        var example = definition.getString("example")
        var link = definition.getString("permalink")

        Resource.sendMessage(chat, "Definition for ${word} by ${author}:\n" +
                "${def}\n${example}\n" +
                "${ups} Thumbs up, ${downs} Thumbs down")
        Resource.sendMessage(chat, link)
    }
}