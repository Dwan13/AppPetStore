package com.project.apppetstore.data.repository

import com.project.apppetstore.data.model.ChatMessage
import com.project.apppetstore.data.model.Pet
import com.project.apppetstore.data.model.Product
import com.project.apppetstore.data.model.Service

interface PetShopRepository {
    fun getServices(): List<Service>
    fun getProducts(): List<Product>
    fun getPets(): List<Pet>
    fun getInitialChat(): List<ChatMessage>
}

