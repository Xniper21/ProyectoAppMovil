package com.levelupgamer.store.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectoappmovil.R
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(username: String, onAddProduct: () -> Unit, onEditProduct: (Int) -> Unit, onLogout: () -> Unit) {
    val viewModel: AdminProductViewModel = viewModel()
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.admin_panel)) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = stringResource(id = R.string.logout))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_product))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Hola, $username",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.titleLarge)
                                val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
                                format.maximumFractionDigits = 0
                                Text(
                                    text = format.format(product.price),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row {
                                IconButton(onClick = { onEditProduct(product.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.edit_product))
                                }
                                IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.remove_item), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
