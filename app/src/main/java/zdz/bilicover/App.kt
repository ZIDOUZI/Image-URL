package zdz.bilicover

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate() // Injection happens in super.onCreate()
        // Use bar
    }
}