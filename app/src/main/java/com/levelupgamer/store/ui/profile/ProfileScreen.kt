package com.levelupgamer.store.ui.profile

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.proyectoappmovil.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) { // Added onLogout parameter
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bm ->
            bitmap = bm
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier.size(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = stringResource(id = R.string.profile),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(id = R.string.profile),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (cameraPermissionState.status.isGranted) {
                    takePictureLauncher.launch(null)
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text(stringResource(id = R.string.change_photo))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        TextButton(onClick = onLogout) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(id = R.string.logout), color = MaterialTheme.colorScheme.error)
        }
    }
}
