package com.project.apppetstore.data.repository

import com.project.apppetstore.R
import com.project.apppetstore.data.model.ChatMessage
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.model.Product
import com.project.apppetstore.data.model.Service

object MockPetShopRepository : PetShopRepository {

    override fun getServices(): List<Service> = listOf(

        // ── Clínicas Veterinarias ─────────────────────────────────────────────
        // supportsDelivery = true en clínicas que ofrecen visita veterinaria a domicilio
        Service(
            id = "c1", name = "Clínica Vet Central",
            category = "Clinicas", description = "Urgencias 24/7 y hospitalización",
            rating = 4.9, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6420, lng = -74.0588,
            supportsDelivery = true   // ofrece visita a domicilio
        ),
        Service(
            id = "c2", name = "Fauna Clínica Usaquén",
            category = "Clinicas", description = "Consulta, vacunación y laboratorio",
            rating = 4.7, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6897, lng = -74.0425,
            supportsDelivery = true   // ofrece visita a domicilio
        ),
        Service(
            id = "c3", name = "Vetpet Suba Colina",
            category = "Clinicas", description = "Laboratorio clínico y ecografía",
            rating = 4.6, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.7340, lng = -74.0916
            // solo cita presencial (requiere equipos de laboratorio)
        ),
        Service(
            id = "c4", name = "Hospital Veterinario Sur",
            category = "Clinicas", description = "Cirugía y hospitalización",
            rating = 4.8, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6291, lng = -74.1559
            // solo cita presencial (cirugía requiere instalaciones)
        ),
        Service(
            id = "c5", name = "Animal Clínic Teusaquillo",
            category = "Clinicas", description = "Dermatología y rehabilitación",
            rating = 4.5, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6512, lng = -74.0913
            // solo cita presencial
        ),
        Service(
            id = "c6", name = "Vet & Más Engativá",
            category = "Clinicas", description = "Medicina preventiva y vacunas",
            rating = 4.4, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.7038, lng = -74.1132,
            supportsDelivery = true   // ofrece visita a domicilio
        ),

        // ── Spas y Peluquerías — solo cita presencial (necesitan instalaciones) ──
        Service(
            id = "s1", name = "Spa Patitas Felices",
            category = "Spa", description = "Baño, peluquería y aromaterapia",
            rating = 4.8, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6673, lng = -74.0836
        ),
        Service(
            id = "s2", name = "Pet Grooming Chicó",
            category = "Spa", description = "Grooming premium y estilismo de razas",
            rating = 4.9, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6739, lng = -74.0432
        ),
        Service(
            id = "s3", name = "Peluquería Canina Suba",
            category = "Spa", description = "Corte, baño y desparasitación",
            rating = 4.5, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.7200, lng = -74.0950
        ),
        Service(
            id = "s4", name = "Baño & Corte Fontibón",
            category = "Spa", description = "Baño medicado y limpieza de oídos",
            rating = 4.3, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6800, lng = -74.1480
        ),
        Service(
            id = "s5", name = "Spa Mascota Palermo",
            category = "Spa", description = "Masajes relajantes y baño de ozono",
            rating = 4.7, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6338, lng = -74.0762
        ),
        Service(
            id = "s6", name = "Stilos Pet Bosa",
            category = "Spa", description = "Servicio económico sin cita previa",
            rating = 4.2, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6000, lng = -74.1750
        ),

        // ── A domicilio — todos soportan domicilio (es su especialidad) ──────
        Service(
            id = "d1", name = "Dr. Carlos Ruiz",
            category = "A domicilio", description = "Veterinario a domicilio — consulta y vacunas",
            rating = 4.9, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6351, lng = -74.0636,
            supportsDelivery = true
        ),
        Service(
            id = "d2", name = "Diego Reina — Cuidador",
            category = "A domicilio", description = "Cuidado de mascotas en tu hogar",
            rating = 4.7, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6476, lng = -74.0810,
            supportsDelivery = true
        ),
        Service(
            id = "d3", name = "Vet Domicilio Salitre",
            category = "A domicilio", description = "Urgencias veterinarias a domicilio",
            rating = 4.6, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6619, lng = -74.1128,
            supportsDelivery = true
        ),
        Service(
            id = "d4", name = "Cuidado Express Modelia",
            category = "A domicilio", description = "Cuidado, paseos y hospedaje en casa",
            rating = 4.5, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6802, lng = -74.1242,
            supportsDelivery = true
        ),
        Service(
            id = "d5", name = "Vet Norte Cedritos",
            category = "A domicilio", description = "Consulta preventiva y laboratorio",
            rating = 4.8, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.7165, lng = -74.0575,
            supportsDelivery = true
        ),
        Service(
            id = "d6", name = "Dr. Pérez — La Macarena",
            category = "A domicilio", description = "Especialista en felinos y aves",
            rating = 4.6, distanceKm = 0.0,
            imageRes = R.drawable.img_cuidador,
            lat = 4.6148, lng = -74.0697,
            supportsDelivery = true
        )
    )

    override fun getProducts(): List<Product> = listOf(
        Product(
            id = "1", name = "Comida Premium", category = "Comida", price = "$24.99",
            imageRes = R.drawable.img_comida,
            description = "Alimento balanceado de alta calidad elaborado con proteínas naturales, vitaminas y minerales esenciales para la salud de tu mascota. Sin conservantes artificiales.",
            rating = 4.8, reviewCount = 312, stock = 25, discount = 0,
            tags = listOf("Natural", "Sin conservantes", "Alto en proteínas")
        ),
        Product(
            id = "2", name = "Juguete de Goma", category = "Juguetes", price = "$9.99",
            imageRes = R.drawable.img_juguetes,
            description = "Juguete resistente de goma no tóxica, ideal para perros de todos los tamaños. Estimula el juego y reduce la ansiedad. Apto para mordedores.",
            rating = 4.5, reviewCount = 187, stock = 40, discount = 15,
            tags = listOf("Resistente", "No tóxico", "Para todas las razas")
        ),
        Product(
            id = "3", name = "Cama Cómoda", category = "Hogar", price = "$39.99",
            imageRes = R.drawable.img_gym,
            description = "Cama ortopédica con espuma viscoelástica que se adapta al cuerpo de tu mascota. Funda lavable en lavadora. Disponible en talla única para medianos y grandes.",
            rating = 4.7, reviewCount = 98, stock = 8, discount = 20,
            tags = listOf("Ortopédica", "Lavable", "Talla M/L")
        ),
        Product(
            id = "4", name = "Tazón de Viaje", category = "Accesorios", price = "$8.49",
            imageRes = R.drawable.img_comida,
            description = "Tazón plegable de silicona alimentaria perfecta para paseos y viajes. Se plega en segundos y cabe en cualquier bolso. Capacidad 350 ml.",
            rating = 4.3, reviewCount = 54, stock = 60, discount = 0,
            tags = listOf("Plegable", "Silicona", "350 ml")
        ),
        Product(
            id = "5", name = "Correa Suave", category = "Accesorios", price = "$15.00",
            imageRes = R.drawable.img_correa,
            description = "Correa ergonómica con mango acolchado antideslizante. Material nylon reforzado de alta resistencia. Longitud 1.5 m, ancho 2 cm. Para perros hasta 40 kg.",
            rating = 4.6, reviewCount = 231, stock = 18, discount = 10,
            tags = listOf("Acolchada", "Nylon reforzado", "Hasta 40 kg")
        )
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
        ChatMessage("1", "¡Hola! Quiero adoptar un perro amigable.", true),
        ChatMessage("2", "¡Genial! Actualmente tenemos disponibles a Luna y Rocky.", false),
        ChatMessage("3", "¿Puedo agendar una visita este fin de semana?", true),
        ChatMessage("4", "Sí, podemos reservar el sábado a las 10 AM para ti.", false)
    )
}
