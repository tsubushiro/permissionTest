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
        AppScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PermissionTestTheme {
        Greeting("Android")
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppScreen() {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(permissionState.status) {
        Log.d("AppScreen", "LaunchedEffect:${permissionState.status.toString()}")
        if(!permissionState.status.isGranted && !permissionState.status.shouldShowRationale)
        {
            permissionState.launchPermissionRequest()
        }
    }
    when {
        permissionState.status.isGranted -> Text("Granted!")
        permissionState.status is PermissionStatus.Denied -> {
            if(permissionState.status.shouldShowRationale) {
                PermissionRationaleDialog {
                    permissionState.launchPermissionRequest()
                }
            }else{
                Column {
                    Text("Denied...Please request again.")
                    TextButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Request")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRationaleDialog(onDialogResult: ()->Unit) {
    AlertDialog(
        text = { Text("Rationale") },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onDialogResult) {
                Text("OK")
            }
        }
    )
}