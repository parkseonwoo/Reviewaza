package com.app.service.reviewaza.login

data class UserItem (
     val userId: String? = null,
     val username: String? = null,
     val userPaaword: String? = null,
     val userPhoneNumber: String? = null,
     val userImage: String? = null,
     val description: String? = null,
     val fcmToken: String? = null,
     var latitude: Double? = null,
     var longitude: Double? = null,
)