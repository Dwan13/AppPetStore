package com.project.apppetstore.data.model

data class UserProfile(
    val fullName: String,
    val email: String,
    val profilePhotoUri: String? = null,
    val petProfile: PetProfile = PetProfile()
)

data class PetProfile(
    val name: String = "Bobby",
    val species: String = "Perro",
    val age: String = "3 años",
    val photoUri: String? = null,
    val traits: List<String> = emptyList()
)

