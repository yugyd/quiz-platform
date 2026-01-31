package com.yugyd.quiz.domain.courses.model

data class CourseDetailModel(
    val id: Int,
    val name: String,
    val description: String,
    val content: String,
    val icon: String?,
    val parentCourseId: Int?,
    val isDetail: Boolean,
)
