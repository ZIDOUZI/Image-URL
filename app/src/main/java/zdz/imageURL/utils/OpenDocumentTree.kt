package zdz.imageURL.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

object OpenDocumentTree : ActivityResultContract<Unit, (Context, Uri?) -> Unit>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(FLAGS)
    
    private const val FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    
    override fun parseResult(resultCode: Int, intent: Intent?): (Context, Uri?) -> Unit =
        lambda@{ ctx, uri ->
            val it = intent.takeIf { resultCode == Activity.RESULT_OK }?.data ?: return@lambda
            if (uri != it && uri != null) {
                try {
                    ctx.contentResolver.releasePersistableUriPermission(uri, FLAGS)
                } catch (e: Exception) {
                    error(e)
                }
            }
            ctx.contentResolver.takePersistableUriPermission(it, FLAGS)
        }
}