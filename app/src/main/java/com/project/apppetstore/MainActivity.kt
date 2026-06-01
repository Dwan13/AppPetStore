package com.project.apppetstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // AppPetStoreTheme se aplica dentro de AppPetShopApp
            // para que el SettingsViewModel controle el tema oscuro/claro
            AppPetShopApp()
        }
    }
}
