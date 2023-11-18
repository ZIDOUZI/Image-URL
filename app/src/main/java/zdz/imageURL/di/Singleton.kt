package zdz.imageURL.di

import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import zdz.imageURL.utils.dataStore

@Module
@InstallIn(SingletonComponent::class)
object Singleton {
    @Provides
    fun provideDataStorePreferences(@ApplicationContext context: Context) =
        context.dataStore
    
    @Provides
    fun provideDownloadManager(@ApplicationContext context: Context) =
        context.getSystemService(DownloadManager::class.java)!!
    
    @Provides
    fun provideClipboard(@ApplicationContext context: Context) =
        context.getSystemService(ClipboardManager::class.java)!!
}