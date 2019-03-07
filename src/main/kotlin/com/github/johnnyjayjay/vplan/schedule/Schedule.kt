package com.github.johnnyjayjay.vplan.schedule

import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Schedule constructor(private val substitutions: Collection<Substitution>, val date: LocalDate) : Collection<Substitution> {

    override val size: Int
        get() = substitutions.size

    override fun contains(element: Substitution) = substitutions.contains(element)

    override fun containsAll(elements: Collection<Substitution>) = substitutions.containsAll(elements)

    override fun isEmpty() = substitutions.isEmpty()

    override fun iterator() = substitutions.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Schedule

        if (substitutions != other.substitutions) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = substitutions.hashCode()
        result = 31 * result + date.hashCode()
        return result
    }

    companion object {

        val EMPTY = Schedule(emptyList(), LocalDate.MIN)
        val columns: Array<String> = arrayOf("Kl.", "Std.", "Fach", "Raum", "VLehrer", "VFach", "VRaum", "Info")
        val dateParser = DateTimeFormatter.ofPattern("eeee dd.MM.yyyy", Locale.GERMAN)

        fun parse(html: Document): Schedule {
            val dateSection = html.select("h2")
            val date = LocalDate.parse(dateSection.text(), dateParser)
            val table = html.select("table")

            if (table.isEmpty())
                return Schedule(emptyList(), date)

            val substitutions: ArrayList<Substitution> = arrayListOf()

            var currentGrade = ""
            var currentHour = ""


            val rows = table.select("tr")
            for (index in rows.indices) {
                val entries = rows[index].select("td")
                if (columns.size != entries.size)
                    throw AssertionError("Column size changed")

                if (entries[0].text() == "Kl.")
                    continue

                val rawGrade = entries[0].text().trim()
                val rawHour = entries[1].text().trim()

                if (!rawGrade.isEmpty())
                    currentGrade = rawGrade

                if (!rawHour.isEmpty())
                    currentHour = rawHour

                val hour = currentHour.toInt()
                val subject = entries[2].text().trim()
                val substituteTeacher = entries[4].text().trim()
                val substituteSubject = entries[5].text().trim()
                val substituteRoom = entries[6].text().trim()
                var information = entries[7].text().trim()

                if (information.startsWith("anstatt"))
                    continue

                val type =
                        if (substituteTeacher == "*Frei") SubstitutionType.CANCELLATION
                        else if (substituteTeacher == "Raumänderung") SubstitutionType.ROOM_CHANGE
                        else if (information == "Zeugnisausgabe") {
                            information = ""
                            SubstitutionType.REPORT_DISTRIBUTION
                        } else if ("verschoben auf" in information) {
                            information += rows[index + 1].select("td")[7].text().trim()
                            SubstitutionType.COURSE_MOVE
                        } else SubstitutionType.DEFAULT

                val notEmpty = currentGrade.isNotEmpty()
                substitutions.add(
                        Substitution(type,
                                if (notEmpty) currentGrade.replace("[a-g]".toRegex(), "").toInt() else 0,
                                if (notEmpty && currentGrade.last() in 'a'..'g') currentGrade.last().toString() else "",
                                hour, information,
                                subject, substituteTeacher,
                                substituteSubject, substituteRoom)
                )

            }

            return Schedule(substitutions, date)
        }

    }
}