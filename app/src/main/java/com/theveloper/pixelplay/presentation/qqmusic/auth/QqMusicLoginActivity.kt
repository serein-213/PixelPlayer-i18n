package com.theveloper.pixelplay.presentation.qqmusic.auth

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.theveloper.pixelplay.ui.theme.PixelPlayTheme
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class QqMusicLoginActivity : ComponentActivity() {
    companion object {
        const val TARGET_URL = "https://y.qq.com/"
        const val DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Safari/537.36"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelPlayTheme {
                QqMusicLoginScreen(onClose = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QqMusicLoginScreen(
    viewModel: QqMusicLoginViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val loginState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var webView by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
            webView = null
        }
    }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is QqMusicLoginState.Success -> onClose()
            is QqMusicLoginState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearError()
            }
            else -> Unit
        }
    }

    BackHandler(enabled = true) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onClose()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QQ Music Login") },
                navigationIcon = {
                    TextButton(onClick = onClose) { Text("Close") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                factory = {
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString = QqMusicLoginActivity.DESKTOP_UA
                        webChromeClient = WebChromeClient()
                        webViewClient = WebViewClient()
                        loadUrl(QqMusicLoginActivity.TARGET_URL)
                    }.also { webView = it }
                }
            )

            Button(
                onClick = {
                    val cookieJson = readQqMusicCookies()
                    viewModel.processCookies(cookieJson)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (loginState is QqMusicLoginState.Loading) "Saving..." else "Done")
            }
        }
    }
}

private fun readQqMusicCookies(): String {
    val manager = CookieManager.getInstance()
    val map = mutableMapOf<String, String>()

    // Gather cookies from all QQ Music hosts used by API calls.
    val cookieHosts = listOf(
        "https://y.qq.com/",
        "https://u6.y.qq.com/",
        "https://u.y.qq.com/",
        "https://c.y.qq.com/"
    )
    cookieHosts.forEach { host ->
        val raw = manager.getCookie(host).orEmpty()
        raw.split(';').forEach { pair ->
            val key = pair.substringBefore('=', "").trim()
            val value = pair.substringAfter('=', "").trim()
            if (key.isNotBlank() && value.isNotBlank()) {
                map[key] = value
            }
        }
    }

    return JSONObject(map as Map<*, *>).toString()
}
