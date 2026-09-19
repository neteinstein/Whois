package com.neteinstein.whois

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.neteinstein.whois.app.App
import com.neteinstein.whois.core.ui.navigation.Navigator
import com.neteinstein.whois.feature.search.domain.IncomingShareBus
import org.koin.android.ext.android.getKoin

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingShareIntent(intent)

        setContent {
            val navigator = remember { getKoin().get<Navigator>() }
            val backStack by navigator.backStack.collectAsState()

            BackHandler(enabled = backStack.size > 1) {
                navigator.popBackStack()
            }

            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShareIntent(intent)
    }

    /**
     * The OS Contacts/People app sends a shared contact as `ACTION_SEND` with the vCard either
     * inline (`EXTRA_TEXT`) or as a `content://` stream (`EXTRA_STREAM`), depending on the vendor.
     * We hand the raw vCard text to [IncomingShareBus]; parsing (and dropping `NOTE`) happens in
     * `:feature:search`'s `ParseSharedContactUseCase`, which the shared `SearchViewModel` already
     * collects from on init, so this just needs to get the raw text there.
     */
    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent == null || intent.action != Intent.ACTION_SEND) return
        val rawVCardText = extractVCardText(intent) ?: return
        getKoin().get<IncomingShareBus>().submit(rawVCardText)
    }

    private fun extractVCardText(intent: Intent): String? {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { return it }

        val streamUri = intent.parcelableExtraCompat(Intent.EXTRA_STREAM, Uri::class.java) ?: return null
        return runCatching {
            contentResolver.openInputStream(streamUri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
    }
}
