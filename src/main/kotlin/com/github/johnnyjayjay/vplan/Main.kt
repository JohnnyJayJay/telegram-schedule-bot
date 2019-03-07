package com.github.johnnyjayjay.vplan

import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
import okhttp3.logging.HttpLoggingInterceptor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    log("Starting bot")
    val connector = HomepageConnector(USERNAME, PASSWORD)
    val updater: Updater = Updater(connector)
    val bot = bot {
        logLevel = HttpLoggingInterceptor.Level.NONE
        token = TOKEN
        dispatch {
            command("start", ::startCommand)
            command("stop", ::stopCommand)
            command("grade") { bot, update -> gradeCommand(bot, update, updater)}
            command("general_notify", ::generalNotifyCommand)
            command("full") { bot, update -> fullShowCommand(bot, update, updater)}
            command("register", ::registerCommand)
        }
    }.also { it.startPolling() }
    updater.bot = bot
    updater.startAutomaticUpdates()
}

fun log(message: Any) = println("${currentTime()} - $message")

private fun currentTime() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss"))

