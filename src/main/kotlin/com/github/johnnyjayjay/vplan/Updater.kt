package com.github.johnnyjayjay.vplan

import com.github.johnnyjayjay.vplan.format.phrase
import com.github.johnnyjayjay.vplan.schedule.Schedule
import me.ivmg.telegram.Bot
import me.ivmg.telegram.network.fold
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("eeee, dd.MM.yyy", Locale.GERMAN)

class Updater(private val connector: HomepageConnector, private val bot: Bot) {

    private var timer: Timer? = null
    private var currentSchedules = connector.loadSchedules()

    fun startAutomaticUpdates() {
        timer = fixedRateTimer(period = PERIOD.toLong(), action = task@{
            val newSchedules = connector.loadSchedules()
            if (newSchedules == currentSchedules)
                return@task

            val updates = ArrayList<Schedule>()
            for (index in (if (newSchedules.size > currentSchedules.size) currentSchedules else newSchedules).indices) {
                val (newSchedule, oldSchedule) = newSchedules[index] to currentSchedules[index]
                updates.add(differenceSchedule(oldSchedule, newSchedule))
            }
            subscribers().forEach { notify(it, updates) }
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
                        bot.sendMessage(chatId = chatId, text = "${formatter.format(schedule.date)}:\n${if (message.isEmpty()) "Keine Einträge!" else message}")
                                .fold { it.exception?.printStackTrace() }
                    }
        }
    }

    private fun differenceSchedule(oldSchedule: Schedule, newSchedule: Schedule): Schedule =
            if (oldSchedule == newSchedule) Schedule(emptyList(), newSchedule.date)
            else Schedule(ArrayList(newSchedule).also { it.removeAll(oldSchedule) }, newSchedule.date)
}