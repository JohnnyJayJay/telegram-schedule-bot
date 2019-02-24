package com.github.johnnyjayjay.vplan.format

import com.github.johnnyjayjay.vplan.schedule.Substitution
import com.github.johnnyjayjay.vplan.schedule.SubstitutionType.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private val moveDateParser: DateTimeFormatter = DateTimeFormatter.ofPattern("eeeee w [dd.MM]", Locale.GERMAN)
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("eeee, dd.MM, w. 'Stunde'", Locale.GERMAN)

fun phrase(substitution: Substitution) = with(substitution) {
    "$hour. Stunde - ${when (type) {
        REPORT_DISTRIBUTION -> "Zeugnisausgabe bei ${teacherFor(substituteTeacher)} in ${roomFor(substituteRoom)}"
        ROOM_CHANGE -> "${subjectFor(subject)} findet in ${roomFor(substituteRoom)} statt"
        CANCELLATION -> "${subjectFor(subject)} entfällt"
        COURSE_MOVE -> {
            val parts = information.split("anstatt")
            val movedTo = LocalDate.parse(parts[0], moveDateParser)
            val insteadOf = LocalDate.parse(parts[1], moveDateParser)
            """${subjectFor(subject)} wird verschoben auf ${dateFormatter.format(movedTo)}.
                |Statt am ${dateFormatter.format(insteadOf)} findet ${subjectFor(substituteSubject)} nun
                |${if (hasSubstituteTeacher) "bei ${teacherFor(substituteTeacher)} " else ""}
                |${if (hasSubstituteRoom) "in ${roomFor(substituteRoom)} " else ""}statt."""
                    .trimMargin().replace("\n", "")
        }
        DEFAULT -> """${teacherFor(substituteTeacher)} vertritt ${subjectFor(subject)}
            |${if (hasSubstituteSubject) " mit ${subjectFor(substituteSubject)}" else ""}
            |${if (hasSubstituteRoom) " in ${roomFor(substituteRoom)}" else ""}"""
                .trimMargin().replace("\n", "")
    }}${if (hasAdditionalInformation) "\nWeitere Informationen: $information" else ""}".trim()
}


