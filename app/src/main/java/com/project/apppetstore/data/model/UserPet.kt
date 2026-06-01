package com.project.apppetstore.data.model

data class UserPet(
    val id        : String       = "",
    val name      : String       = "",
    val species   : String       = "Perro",
    val age       : String       = "",
    val photoUri  : String?      = null,   // URL de Firebase Storage
    val traits    : List<String> = emptyList(),
    val createdAt : Long         = System.currentTimeMillis()
)
