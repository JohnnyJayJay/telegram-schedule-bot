package com.github.johnnyjayjay.vplan.schedule

data class Substitution(
        val type: SubstitutionType,
        val grade: Int,
        val gradeIdentifier: String,
        val hour: Int,
        val information: String,
        val subject: String,
        val substituteTeacher: String,
        val substituteSubject: String,
        val substituteRoom: String
) {

    val hasGrade: Boolean
        get() = grade != 0

    val fullGrade: String
        get() = "$grade$gradeIdentifier"

    val hasSubstituteTeacher: Boolean
        get() = !substituteTeacher.isEmpty()

    val hasSubstituteRoom: Boolean
        get() = !substituteRoom.isEmpty()

    val hasSubstituteSubject: Boolean
        get() = !substituteSubject.isEmpty()

    val hasAdditionalInformation: Boolean
        get() = !information.isEmpty()
}

enum class SubstitutionType {
    DEFAULT,
    ROOM_CHANGE,
    CANCELLATION,
    REPORT_DISTRIBUTION,
    COURSE_MOVE
}
