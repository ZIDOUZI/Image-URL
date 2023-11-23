package zdz.imageURL.receive

/*
@AndroidEntryPoint
class DownloadReceiver @Inject constructor(
    private val downloader: DownloadManager,
    private val pf: Prefer,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (id == -1L) return
        val cursor = downloader.query(DownloadManager.Query().setFilterById(id))
        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        if (status != DownloadManager.STATUS_SUCCESSFUL) return
        
        val uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        context.viewContent(
            Uri.parse(uri),
            mimeType = "application/vnd.android.package-archive",
            choose = pf.installerChooser.value
        )
        cursor.close()
    }
}*/
// TODO: cannot compile
