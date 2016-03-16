package io.mazenmc.skypebot

import io.mazenmc.skypebot.engine.bot.ModuleManager
import io.mazenmc.skypebot.modules.General
import io.mazenmc.skypebot.stat.StatisticsManager
import io.mazenmc.skypebot.utils.UpdateChecker

fun main(args: Array<String>) {
    SkypeBot.loadThirdParty()
    SkypeBot.loadConfig()
    SkypeBot.loadSkype()
    General.load()
    StatisticsManager.loadStatistics()
    ModuleManager.loadModules("io.mazenmc.skypebot.modules")
    UpdateChecker().start()
}