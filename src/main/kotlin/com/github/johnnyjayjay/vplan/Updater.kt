package com.github.johnnyjayjay.vplan

import com.github.johnnyjayjay.vplan.format.phrase
import com.github.johnnyjayjay.vplan.schedule.Schedule
import me.ivmg.telegram.Bot
import me.ivmg.telegram.network.fold
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("eeee, dd.MM.yyy", Locale.GERMAN)

class Updater(private val connector: HomepageConnector) {

    var bot: Bot? = null
    private var timer: Timer? = null
    private var currentSchedules = emptyList<Schedule>()

    fun startAutomaticUpdates() {
        log("Starting automatic updates")
        timer = fixedRateTimer(period = PERIOD.toLong(), action = task@{
            val newSchedules = connector.loadSchedules()
            if (newSchedules == currentSchedules)
                return@task

            log("New schedules")
            val updates = ArrayList<Schedule>()
            for (index in newSchedules.indices) {
                val (newSchedule, oldSchedule) = newSchedules[index] to (if (currentSchedules.size > index) currentSchedules[index] else Schedule.EMPTY)
                updates.add(differenceSchedule(oldSchedule, newSchedule))
            }
            subscribers().forEach { notify(it, updates) }
            currentSchedules = newSchedules
        })
    }

    fun stop() = timer?.cancel()

    fun notify(subscriber: Subscriber, schedules: Collection<Schedule> = currentSchedules) {
        with(subscriber) {
            schedules.filter { it.isNotEmpty() }
                    .forEach { schedule ->
                        val message = schedule.filter {
                            it.fullGrade == fullGrade
                                    || it.fullGrade == grade.toString()
                                    || (!it.hasGrade && receiveGeneralNotifications)
                        }.map { phrase(it) }.joinToString("\n\n")
                        bot?.sendMessage(chatId = chatId, text = "${formatter.format(schedule.date)}:\n${if (message.isEmpty()) "Keine Einträge!" else message}")
                                ?.fold { it.exception?.printStackTrace() }
                    }
        }
    }

    private fun differenceSchedule(oldSchedule: Schedule, newSchedule: Schedule): Schedule =
            if (oldSchedule == newSchedule) Schedule(emptyList(), newSchedule.date)
            else Schedule(ArrayList(newSchedule).also { it.removeAll(oldSchedule) }, newSchedule.date)
}