package io.mazenmc.skypebot.engine.bot.generic

import com.samczsun.skype4j.chat.messages.ReceivedMessage

class MethodResponse(val responder: Responder): StringResponse("N/A") {
    override fun process(message: ReceivedMessage): String {
        super.process(message)
        return responder.process(message, lastArgs.toTypedArray())
    }
}