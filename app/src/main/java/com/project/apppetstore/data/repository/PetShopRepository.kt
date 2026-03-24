package com.project.apppetstore.data.repository

import com.project.adopetshop.data.model.ChatMessage
import com.project.adopetshop.data.model.Pet
import com.project.adopetshop.data.model.Product
import com.project.adopetshop.data.model.Service

interface PetShopRepository {
    fun getServices(): List<Service>
    fun getProducts(): List<Product>
    fun getPets(): List<Pet>
    fun getInitialChat(): List<ChatMessage>
}

