package com.github.johnnyjayjay.vplan

import com.github.johnnyjayjay.vplan.schedule.Schedule
import org.jsoup.Jsoup
import java.lang.AssertionError
import java.util.*

class HomepageConnector(username: String, password: String) {

    val authorization: String = encodeBase64(username, password)

    fun loadSchedules(): List<Schedule> {
        val selectionHTML = getHTML(URL)
        val links = selectionHTML.select("a[href^=\"V\"]")
        if (links.isEmpty())
            throw AssertionError("There are no schedules")

        return links
                .map { it.absUrl("href") }
                .map { getHTML(it) }
                .map { Schedule.parse(it) }
    }

    private fun getHTML(url: String) =
            java.net.URL(url).openConnection()
                    .also { it.setRequestProperty("Authorization", "Basic $authorization") }
                    .getInputStream().use { Jsoup.parse(it, "ISO8859-1", url) }

}

private fun encodeBase64(username: String, password: String) =
        String(Base64.getEncoder().encode("$username:$password".toByteArray()))

fun testCredentials(username: String, password: String) =
        try {
            java.net.URL(URL).openConnection()
                    .also { it.setRequestProperty("Authorization", "Basic ${encodeBase64(username, password)}") }
                    .getInputStream()?.close()
            true
        } catch (e: Exception) {
            false
        }

