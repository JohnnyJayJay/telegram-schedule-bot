package com.github.johnnyjayjay.vplan

import me.ivmg.telegram.Bot
import me.ivmg.telegram.entities.Update
import me.ivmg.telegram.network.fold

fun startCommand(bot: Bot, update: Update) {
    val chatId = chatId(update)
    val response = if (isRegistered(chatId)) """
                    |Hi! Diese Commands kannst du nutzen:
                    |/full - Zeige deinen vollen Vertretungsplan an.
                    |/stop - Du erhältst keine Updates mehr.
                    |/grade [klasse] - Ändere deine Klasse.
                    |/general_notify [an|aus] - Schalte allgemeine Benachrichtigungen (für AGs usw.) an/aus.
                """.trimMargin()
    else """
                    |Hi! Du bist noch nicht registriert. Bitte nutze "/register benutzername:passwort" um dich zu verifizieren (die Daten werden nicht gespeichert).
                    |Wenn alles klappt, kannst du danach deine Klasse mit "/grade [klasse]" auswählen und du erhältst Benachrichtigungen.
                    |Du erhältst jedes Mal eine Nachricht, wenn sich auf dem Vertretungsplan etwas ändert.
                """.trimMargin()
    bot.sendMessage(chatId = chatId, text = response).fold { it.exception?.printStackTrace() }
}

fun stopCommand(bot: Bot, update: Update) {
    val chatId = chatId(update)
    val response = if (isRegistered(chatId)) "Du erhältst nun keine Benachrichtigungen mehr." else "Du bist nicht registriert."
    removeSubscriber(chatId)
    saveSubscribers()
    bot.sendMessage(chatId = chatId, text = response).fold { it.exception?.printStackTrace() }
}

fun gradeCommand(bot: Bot, update: Update, updater: Updater) {
    val chatId = chatId(update)
    val message = update.message!!.text!!.replaceFirst("/grade ", "")
    val response = if (isRegistered(chatId)) {
        if (message.matches("(1[1-2])|((10)|([5-9])[a-g])".toRegex())) {
            val grade = message.replace("[a-g]".toRegex(), "").toInt()
            val identifier = if (message.last() in 'a'..'g') message.last().toString() else ""
            updateSubscriber(findSubscriber(chatId), newGrade = grade, newIdentifier = identifier)
            saveSubscribers()
            updater.notify(findSubscriber(chatId))
            "Deine Klasse wurde geändert!"
        } else "Das ist keine gültige Klasse."
    } else "Du bist nicht registriert."
    bot.sendMessage(chatId = chatId, text = response).fold { it.exception?.printStackTrace() }
}

fun generalNotifyCommand(bot: Bot, update: Update) {
    val chatId = chatId(update)
    val message = update.message!!.text!!.replaceFirst("/general_notify ", "")
    val response = if (isRegistered(chatId)) {
        val current = findSubscriber(chatId)
        val (response, newSetting) = when (message) {
            "an" -> "Allgemeine Benachrichtigungen aktiviert" to true
            "aus" -> "Allgemeine Benachrichtigungen deaktiviert" to false
            else -> "Das ist nicht gültig. Bitte gib \"an\" oder \"aus\" an" to current.receiveGeneralNotifications
        }
        updateSubscriber(current, newReceiveGeneral = newSetting)
        saveSubscribers()
        response
    } else "Du bist nicht registriert."
    bot.sendMessage(chatId = chatId, text = response).fold { it.exception?.printStackTrace() }
}

fun fullShowCommand(bot: Bot, update: Update, updater: Updater) {
    val chatId = chatId(update)
    if (!isRegistered(chatId)) {
        bot.sendMessage(chatId = chatId, text = "Du bist nicht registriert.").fold { it.exception?.printStackTrace() }
    } else {
        updater.notify(findSubscriber(chatId))
    }
}

fun registerCommand(bot: Bot, update: Update) {
    val chatId = chatId(update)
    val message = update.message!!.text!!.replaceFirst("/register ", "")
    val parts = message.split(":")
    val response =
            when {
                isRegistered(chatId) -> "Du bist bereits registriert."
                parts.size != 2 -> "Diese Angabe ist ungültig. Bitte nutze das Format \"benutzername:passwort\"."
                else -> {
                    val (username, password) = parts
                    if (testCredentials(username, password)) {
                        addSubscriber(Subscriber(chatId = chatId, grade = -1))
                        saveSubscribers()
                        "Du wurdest erfolgreich verifiziert. Bitte nutze nun \"/grade [klasse]\", um deine Klasse einzutragen."
                    } else {
                        "Der Benutzername oder das Passwort ist falsch!"
                    }
                }
            }
    bot.sendMessage(chatId = chatId, text = response).fold { it.exception?.printStackTrace() }
}

private fun chatId(update: Update) = update.message!!.chat.id