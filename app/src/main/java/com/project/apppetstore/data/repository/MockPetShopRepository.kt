package com.project.apppetstore.data.repository

import com.project.apppetstore.R
import com.project.apppetstore.data.model.ChatMessage
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.model.Product
import com.project.apppetstore.data.model.Service

object MockPetShopRepository : PetShopRepository {

    override fun getServices(): List<Service> = listOf(
        Service(
            id = "1",
            name = "Dr. Carlos Ruiz",
            category = "Veterinario a domicilio",
            description = "Veterinario a domicilio",
            rating = 4.9,
            distanceKm = 2.1,
            imageRes = R.drawable.img_cuidador
        ),
        Service(
            id = "2",
            name = "Pet Spa",
            category = "Baño y peluquería",
            description = "Baño y peluquería",
            rating = 4.4,
            distanceKm = 1.4,
            imageRes = R.drawable.img_cuidador
        ),
        Service(
            id = "3",
            name = "Diego Reina",
            category = "Cuidado de mascotas",
            description = "Cuidado de mascotas",
            rating = 4.7,
            distanceKm = 3.1,
            imageRes = R.drawable.img_cuidador
        ),
        Service(
            id = "4",
            name = "Dr. Carlos Ruiz",
            category = "Veterinario a domicilio",
            description = "Veterinario a domicilio",
            rating = 4.9,
            distanceKm = 2.1,
            imageRes = R.drawable.img_cuidador
        ),
        Service(
            id = "5",
            name = "Pet Spa",
            category = "Baño y peluquería",
            description = "Baño y peluquería",
            rating = 4.4,
            distanceKm = 1.4,
            imageRes = R.drawable.img_cuidador
        ),
        Service(
            id = "6",
            name = "Diego Reina",
            category = "Cuidado de mascotas",
            description = "Cuidado de mascotas",
            rating = 4.7,
            distanceKm = 3.1,
            imageRes = R.drawable.img_cuidador
        )
    )

    override fun getProducts(): List<Product> = listOf(
        Product("1", "Premium Food", "Food", "$24.99", R.drawable.img_comida),
        Product("2", "Rubber Toy", "Toys", "$9.99", R.drawable.img_juguetes),
        Product("3", "Comfort Bed", "Home", "$39.99", R.drawable.img_gym),
        Product("4", "Travel Bowl", "Accessories", "$8.49", R.drawable.img_comida),
        Product("5", "Soft Leash", "Accessories", "$15.00", R.drawable.img_correa)
    )

    override fun getPets(): List<Pet> = listOf(
        Pet(
            id = "1",
            name = "Luna",
            age = "1 año",
            breed = "Gata atigrada",
            gender = "Hembra",
            size = "Mediana",
            health = "Excelente estado de salud",
            vaccines = "Todas las vacunas al día",
            personality = "Amigable, juguetona y energética. Perfecto para familias con niños.",
            requirements = "Recursos económicos para veterinario, alimento de calidad, y un hogar seguro y enriquecido",
            imageRes = R.drawable.img_luna
        ),
        Pet(
            id = "2",
            name = "Max",
            age = "2 años",
            breed = "Golden Retriever",
            gender = "Macho",
            size = "Grande",
            health = "Sano, esterilizado",
            vaccines = "Rabia",
            personality = "Curioso, tranquilo, le gusta observar desde lugares altos",
            requirements = "Ambiente seguro en casa",
            imageRes = R.drawable.img_max
        ),
        Pet(
            id = "3",
            name = "Rocky",
            age = "6 meses",
            breed = "Beagle",
            gender = "Macho",
            size = "Mediano",
            health = "Sano",
            vaccines = "Vacunas al día",
            personality = "Energético, leal",
            requirements = "Ejercicio diario",
            imageRes = R.drawable.img_rocky
        ),
        Pet(
            id = "4",
            name = "Simba",
            age = "8 meses",
            breed = "Gato común europeo",
            gender = "Macho",
            size = "pequeño",
            health = "Sano",
            vaccines = "Vacunas al día",
            personality = "Obediente, le gusta estar en lugares altos",
            requirements = "Ambiente seguro en casa",
            imageRes = R.drawable.img_simba
        )
    )

    override fun getInitialChat(): List<ChatMessage> = listOf(
        ChatMessage("1", "Hi! I want to adopt a friendly dog.", true),
        ChatMessage("2", "Great! We currently have Luna and Rocky available.", false),
        ChatMessage("3", "Can I schedule a visit this weekend?", true),
        ChatMessage("4", "Yes, we can reserve Saturday at 10 AM for you.", false)
    )
}
