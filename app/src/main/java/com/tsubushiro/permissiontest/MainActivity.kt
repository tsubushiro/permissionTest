package com.tsubushiro.permissiontest

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.tsubushiro.permissiontest.ui.theme.PermissionTestTheme
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PermissionTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Permisson",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        PermissionRequiredContent(permission = Manifest.permission.CAMERA){
            Text("Camera permission granted")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PermissionTestTheme {
        Greeting("Android")
    }
}

// 権限の必要なComposableを呼び出す関数
// 使い方：
// PermissionRequiredContent(permission = Manifest.permission.CAMERA){
//            Text("Camera permission granted")
//        }
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequiredContent(
    permission: String,// パーミッション名 Manifest.permission.****
    grantedContent: @Composable () -> Unit //権限取得成功時のComposable
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission)

    // 権限ごとのRationaleメッセージとDeniedメッセージを取得
    val rationaleText = getRationaleText(permission)
    val deniedText = getDeniedText(permission)

    LaunchedEffect(permissionState.status) {
        Log.d("PermissionRequiredContent", "LaunchedEffect:${permissionState.status.toString()}")
        if(!permissionState.status.isGranted && !permissionState.status.shouldShowRationale) {
            permissionState.launchPermissionRequest()
        }
    }

    when {
        permissionState.status.isGranted -> grantedContent() // 権限取得成功
        permissionState.status is PermissionStatus.Denied -> {
            if(permissionState.status.shouldShowRationale) {
                PermissionRationaleDialog(
                    rationaleText = rationaleText,
                    onDialogResult = {
                        permissionState.launchPermissionRequest()
                    }
                )
            } else {
                Column (
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(deniedText)
                    TextButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("設定を開く")
                    }
                }
            }
        }
        else -> SideEffect {
            permissionState.launchPermissionRequest()
        }
    }
}

// 権限ごとのRationaleメッセージを用意
fun getRationaleText(permission: String): String = when(permission) {
    Manifest.permission.CAMERA ->
        "カメラ機能を使用するには、カメラへのアクセス許可が必要です。"
    Manifest.permission.READ_MEDIA_IMAGES,
    Manifest.permission.READ_EXTERNAL_STORAGE ->
        "オーバーレイ画像を選択するには、「ファイルとメディア」へのアクセス許可が必要です。"
    else -> "この機能を利用するには権限が必要です。"
}

// 権限ごとのDeniedメッセージを用意
fun getDeniedText(permission: String): String = when(permission) {
    Manifest.permission.CAMERA ->
        "カメラパーミッションが拒否されました。\nアプリの設定から許可してください。"
    Manifest.permission.READ_MEDIA_IMAGES,
    Manifest.permission.READ_EXTERNAL_STORAGE ->
        "「ファイルとメディア」パーミッションが拒否されました。\nアプリの設定から許可してください。"
    else -> "パーミッションが拒否されました。\nアプリの設定から許可してください。"
}

@Composable
fun PermissionRationaleDialog(
    rationaleText: String,
    onDialogResult: () -> Unit
) {
    AlertDialog(
        text = { Text(rationaleText) },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onDialogResult) {
                Text("許可する")
            }
        }
    )
}
