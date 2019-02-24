package com.github.johnnyjayjay.vplan.format

import com.github.johnnyjayjay.vplan.schedule.Schedule
import org.apache.commons.csv.CSVFormat
import java.io.InputStreamReader

private val teachers: Map<String, String> = csvToMap("/teachers.csv")
private val rooms: Map<String, String> = csvToMap("/rooms.csv")
private val subjects: Map<String, String> = csvToMap("/subjects.csv")

private fun csvToMap(fileName: String): Map<String, String> {
    val results = HashMap<String, String>()
    Schedule::class.java.getResourceAsStream(fileName).use { inputStream ->
        CSVFormat.EXCEL.withDelimiter(';').parse(InputStreamReader(inputStream)).use {
            for (row in it) {
                val abbreviation = row[0].toLowerCase()
                val realName = row[1]
                results[abbreviation] = realName
            }
        }
    }
    return results
}

fun teacherFor(abbreviation: String) = teachers[abbreviation.toLowerCase()] ?: abbreviation

fun subjectFor(abbreviation: String): String {
    with(subjects[abbreviation.toLowerCase()]) {
        if (this != null)
            return this
    }

    var result = abbreviation

    fun withoutFirstLetter() {
        result = result.substring(1, result.length)
    }

    return when {
        abbreviation.startsWith('g') -> "Grundkurs ".also { withoutFirstLetter() }
        abbreviation.startsWith('L') -> "Leistungskurs ".also { withoutFirstLetter() }
        else -> ""
    } + join(result.split("[\\s-]+".toRegex()), subjects)
}

fun roomFor(abbreviation: String): String {
    with(rooms[abbreviation.toLowerCase()]) {
        if (this != null)
            return this
    }

    val split = abbreviation.split("\\s+".toRegex())
    return if (split.size == 1) "Raum ${split[0]}" else join(split, rooms)
}

private fun join(split: List<String>, map: Map<String, String>): String {
    val builder = StringBuilder()
    for (word in split) {
        val fullName = map[word.toLowerCase()]
        builder.append(fullName ?: word).append(" ")
    }
    return builder.toString().trim()
}
