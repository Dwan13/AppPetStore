package com.project.apppetstore.data.model

data class UserPet(
    val id: String = "",
    val name: String = "",
    val species: String = "Perro",
    val age: String = "",
    val gender: String = "",
    val size: String = "",
    val health: String = "",
    val vaccines: String = "",
    val requirements: String = "",
    val photoUri: String? = null,   // URL de Firebase Storage
    val traits: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    /** true cuando el usuario ha publicado esta mascota en el catálogo de adopción. */
    val isAvailableForAdoption: Boolean = false
)
