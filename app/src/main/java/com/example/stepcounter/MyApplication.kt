package com.example.stepcounter

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Устанавливаем API-ключ перед инициализацией MapKit
        MapKitFactory.setApiKey("18c9d38c-7d26-409c-b392-db8dbffaded9")
        MapKitFactory.initialize(this)
    }
}
