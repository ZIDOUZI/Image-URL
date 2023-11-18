package zdz.imageURL.service

import android.content.Intent
import android.service.quicksettings.TileService
import androidx.core.service.quicksettings.PendingIntentActivityWrapper
import androidx.core.service.quicksettings.TileServiceCompat
import zdz.imageURL.activity.main.MainActivity
import zdz.imageURL.utils.Intent

class PasteTileService : TileService() {
    override fun onClick() {
        Intent(this, MainActivity::class.java)
        Intent(Intent.ACTION_PASTE) {
            setClassName(packageName, MainActivity::class.java.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.let {
            PendingIntentActivityWrapper(this, 0, it, 0, false)
        }.let {
            TileServiceCompat.startActivityAndCollapse(this, it)
        }
        this.stopSelf()
    }
}