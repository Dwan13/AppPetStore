package com.project.apppetstore.data.repository

import com.project.adopetshop.R
import com.project.adopetshop.data.model.ChatMessage
import com.project.adopetshop.data.model.Pet
import com.project.adopetshop.data.model.Product
import com.project.adopetshop.data.model.Service

object MockPetShopRepository : PetShopRepository {

    override fun getServices(): List<Service> = listOf(
        Service("1", "Grooming", "Beauty", "Full grooming session for cats and dogs.", R.drawable.img_service_grooming),
        Service("2", "Veterinary Check", "Health", "General consultation and preventive care.", R.drawable.img_service_vet),
        Service("3", "Training", "Behavior", "Basic obedience and social training.", R.drawable.img_service_training),
        Service("4", "Dental Care", "Health", "Oral cleaning and dental recommendations.", R.drawable.img_service_dental),
        Service("5", "Pet Taxi", "Support", "Safe transport for appointments.", R.drawable.img_service_taxi)
    )

    override fun getProducts(): List<Product> = listOf(
        Product("1", "Premium Food", "Food", "$24.99", R.drawable.img_product_food),
        Product("2", "Rubber Toy", "Toys", "$9.99", R.drawable.img_product_toy),
        Product("3", "Comfort Bed", "Home", "$39.99", R.drawable.img_product_bed),
        Product("4", "Daily Shampoo", "Care", "$12.50", R.drawable.img_product_shampoo),
        Product("5", "Travel Bowl", "Accessories", "$8.49", R.drawable.img_product_bowl),
        Product("6", "Soft Leash", "Accessories", "$15.00", R.drawable.img_product_leash)
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

