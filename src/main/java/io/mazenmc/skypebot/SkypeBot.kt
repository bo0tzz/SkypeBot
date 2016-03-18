package io.mazenmc.skypebot

import com.samczsun.skype4j.Skype
import com.samczsun.skype4j.SkypeBuilder
import com.samczsun.skype4j.chat.Chat
import com.samczsun.skype4j.chat.GroupChat
import com.samczsun.skype4j.exceptions.ConnectionException
import com.samczsun.skype4j.exceptions.handler.ErrorHandler
import com.samczsun.skype4j.exceptions.handler.ErrorSource
import com.samczsun.skype4j.formatting.Message
import com.samczsun.skype4j.formatting.Text
import com.samczsun.skype4j.internal.SkypeEventDispatcher
import io.mazenmc.skypebot.utils.Resource
import io.mazenmc.skypebot.utils.Utils
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.awt.Color
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

object SkypeBot {
    private val scheduler:     ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val relogRunnable: Runnable                 = RelogRunnable()
    private val errorHandler:  ErrorHandler             = BotErrHandler()

    private var twitter: Twitter? = null
    private var username:  String? = null
    private var password:  String? = null
    private var groupConv: GroupChat?   = null
    private var skype:     Skype?  = null

    private var listenerMap:  Field        by Delegates.notNull()

    fun loadConfig() {
        var prop = Properties()
        var config = File("bot.properties")

        if (!config.exists()) {
            FileOutputStream(config).use {
                prop.setProperty("username", "your.skype.username")
                prop.setProperty("password", "your.skype.password")
                prop.store(it, null)
            }
            println("Generated default configuration. Exiting")
            System.exit(1)
            return
        }

        FileInputStream(config).use {
            prop.load(it)
            username = prop.getProperty("username")
            password = prop.getProperty("password")
        }
    }

    fun loadSkype() {
        try {
            listenerMap = SkypeEventDispatcher::class.java.getDeclaredField("listeners")
            listenerMap.isAccessible = true
        } catch (e: NoSuchFieldException) {
            e.printStackTrace() // welp rip
            System.exit(0)
            return
        }

        scheduler.scheduleAtFixedRate(relogRunnable, 0, 8, TimeUnit.HOURS)
    }

    fun loadThirdParty() {
        var twitterInfo = Utils.readAllLines("twitter_auth")
        var cb = ConfigurationBuilder()

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(twitterInfo[0])
                .setOAuthConsumerSecret(twitterInfo[1])
                .setOAuthAccessToken(twitterInfo[2])
                .setOAuthAccessTokenSecret(twitterInfo[3])
        twitter = TwitterFactory(cb.build()).instance
    }

    fun groupConv(): GroupChat? {
        if (groupConv == null) {
            groupConv = skype?.getOrLoadChat("19:7cb2a86653594e18abb707e03e2d1848@thread.skype") as GroupChat
        }

        return groupConv
    }

    fun twitter(): Twitter? {
        return twitter
    }

    fun getSkype(): Skype? {
        return skype
    }

    fun sendMessage(message: String) {
        try {
            groupConv()?.sendMessage(message)
        } catch (e: ConnectionException) {
            groupConv = null
            sendMessage(message)
        }
    }

    fun sendMessage(message: Message) {
        try {
            groupConv()?.sendMessage(message)
        } catch (e: ConnectionException) {
            groupConv = null
            sendMessage(message)
        }
    }

    private class RelogRunnable: Runnable {
        override fun run() {
            println("Starting relog process")
            var newSkype: Skype? = null
            var retry = true
            while (retry) {
                try {
                    newSkype = SkypeBuilder(username, password).withAllResources()
                            .withExceptionHandler(errorHandler).build()
                    newSkype.login()
                    println("Logged in with username $username")
                    newSkype.subscribe()
                    println("Successfully subscribed")
                    newSkype.eventDispatcher.registerListener(SkypeEventListener())
                    retry = false
                } catch (t: Throwable) {
                    t.printStackTrace()
                    Thread.sleep(10000)
                }
            }

            groupConv = null
            var oldSkype = skype
            skype = newSkype

            if (oldSkype != null) {
                try {
                    var listeners = listenerMap.get(oldSkype.eventDispatcher)

                    if (listeners is MutableMap<*, *>) {
                        listeners.clear()
                    }
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }

                println("Logging out of old Skype")
                oldSkype.logout()
            } else {
                sendMessage(Message.create().with(Text.rich("Mazen's Bot ${Resource.VERSION} started!").withColor(Color.GREEN)))
            }
        }
    }

    private class BotErrHandler: ErrorHandler {
        override fun handle(errorSource: ErrorSource?, error: Throwable?, shutdown: Boolean) {
            if (shutdown) {
                println("Error detected, relogging: ${error.toString()}")
                skype = null
                scheduler.submit(relogRunnable)
            }
        }
    }
}