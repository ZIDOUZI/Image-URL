package zdz.bilicover

import android.net.Uri
import java.io.File

val Uri.abPath: File?
    get() {
        return this.path?.let {
            File(
                it.replace("tree/primary:", "storage/emulated/0/")
                    .replace("tree/raw:","")
            )
        }
    }