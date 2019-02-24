package com.github.johnnyjayjay.vplan

import com.beust.klaxon.Klaxon
import java.io.File

private val klaxon = Klaxon()
private val file = File("subscribers.json").also {
    if (!it.exists()) {
        it.createNewFile()
        it.writeText("[]")
    }
}

private val subscribers: ArrayList<Subscriber> = ArrayList(klaxon.parseArray(file.useLines { it.joinToString("\n") }))


data class Subscriber(
        val chatId: Long,
        val grade: Int,
        val gradeIdentifier: String = "",
        val receiveGeneralNotifications: Boolean = false
) {
    val fullGrade: String
        get() = "$grade$gradeIdentifier"
}

fun saveSubscribers() = file.writeText(klaxon.toJsonString(subscribers))

fun updateSubscriber(
        oldSubscriber: Subscriber,
        newGrade: Int = oldSubscriber.grade,
        newIdentifier: String = oldSubscriber.gradeIdentifier,
        newReceiveGeneral: Boolean = oldSubscriber.receiveGeneralNotifications
) {
    removeSubscriber(oldSubscriber.chatId)
    subscribers.add(oldSubscriber.copy(grade = newGrade, gradeIdentifier = newIdentifier, receiveGeneralNotifications = newReceiveGeneral))
}

fun removeSubscriber(chatId: Long) = subscribers.removeIf { it.chatId == chatId }

fun addSubscriber(subscriber: Subscriber) = subscribers.add(subscriber)

fun findSubscriber(chatId: Long) = subscribers.first { it.chatId == chatId }

fun isRegistered(id: Long) = id in subscribers.map { it.chatId }

fun subscribers(): List<Subscriber> = subscribers
