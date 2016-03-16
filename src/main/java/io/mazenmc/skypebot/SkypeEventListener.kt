package io.mazenmc.skypebot

import com.samczsun.skype4j.events.EventHandler
import com.samczsun.skype4j.events.Listener
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent
import com.samczsun.skype4j.events.chat.sent.PictureReceivedEvent
import com.samczsun.skype4j.formatting.Message
import com.samczsun.skype4j.formatting.Text
import io.mazenmc.skypebot.engine.bot.ModuleManager
import io.mazenmc.skypebot.stat.StatisticsManager
import io.mazenmc.skypebot.utils.Resource
import io.mazenmc.skypebot.utils.Utils
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

class SkypeEventListener: Listener {
    @EventHandler
    fun onMessage(event: MessageReceivedEvent) {
        ModuleManager.parseText(event.message)
        StatisticsManager.logMessage(event.message)
    }

    @EventHandler
    fun onImage(event: PictureReceivedEvent) {
        var file = File("lastImage.png")

        if (file.exists()) {
            file.delete()
        }

        try {
            if (!SkypeBot.groupConv()!!.allUsers.any { u -> u.username.equals(event.sender.username) }) {
                return; // what you doin? random tryin' to send pics. I dun want your nudes bruh.
            }

            ImageIO.write(event.sentImage, "png", file)
            event.chat.sendMessage(Message.create().with(Text.rich("Uploading image...").withColor(Color.CYAN)))

            var link = Utils.upload(file)

            event.chat.sendMessage(Message.create().with(Text.rich("Uploaded!").withColor(Color.GREEN)))
            Resource.sendMessage("${event.sender.username} sent an image...")
            Resource.sendMessage(link)
        } catch (ex: Exception) {
            event.chat.sendMessage(Message.create().with(Text
                    .rich("ERROR: Unable to send image to group chat").withColor(Color.RED)))
            ex.printStackTrace()
        }
    }
}