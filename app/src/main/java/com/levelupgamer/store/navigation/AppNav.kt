package com.levelupgamer.store.navigation

import android.app.Application
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.levelupgamer.store.data.model.UserRole
import com.levelupgamer.store.data.repository.AuthRepository
import com.levelupgamer.store.ui.admin.AdminPanelScreen
import com.levelupgamer.store.ui.admin.AdminProductViewModel
import com.levelupgamer.store.ui.admin.ProductEditScreen
import com.levelupgamer.store.ui.cart.CartScreen
import com.levelupgamer.store.ui.cart.CartViewModel
import com.levelupgamer.store.ui.home.CategoryScreen
import com.levelupgamer.store.ui.home.HomeScreen
import com.levelupgamer.store.ui.login.ForgotPasswordScreen
import com.levelupgamer.store.ui.login.LoginScreen
import com.levelupgamer.store.ui.login.LoginViewModel
import com.levelupgamer.store.ui.login.RegisterScreen
import com.levelupgamer.store.ui.products.ProductDetailScreen
import com.levelupgamer.store.ui.products.ProductListScreen
import com.levelupgamer.store.ui.profile.ProfileScreen
import com.example.proyectoappmovil.R

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application

    // Create shared ViewModels here, at the top level
    val cartViewModel: CartViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application))
    val adminViewModel: AdminProductViewModel = viewModel()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            // Show BottomBar only on customer screens
            val showBottomBar = currentDestination?.hierarchy?.any { it.route?.startsWith("main_flow") == true } == true

            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    val items = listOf(BottomNavItem.Home, BottomNavItem.Profile, BottomNavItem.Cart)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(id = screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700)) }
        ) {
            // Login, Register, and Forgot Password Flow
            composable("login") {
                val loginViewModel: LoginViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return LoginViewModel(AuthRepository()) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                })
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { user ->
                        val destination = if (user.role == UserRole.ADMIN) "admin_flow/${user.username}" else "main_flow/${user.username}"
                        navController.navigate(destination) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate("register") },
                    onForgotPasswordClick = { navController.navigate("forgot_password") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = { email ->
                        navController.navigate("main_flow/$email") { 
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("forgot_password") {
                ForgotPasswordScreen(
                    onLinkSent = { navController.popBackStack() }, // Go back to login after link is sent
                    onBack = { navController.popBackStack() }
                )
            }

            // Main Customer Flow
            navigation(startDestination = BottomNavItem.Home.route, route = "main_flow/{username}") { 
                composable(BottomNavItem.Home.route) { navBackStackEntry ->
                    val parentEntry = remember(navBackStackEntry) {
                        navController.getBackStackEntry("main_flow/{username}")
                    }
                    val username = parentEntry.arguments?.getString("username") ?: ""
                    HomeScreen(username = username, onViewCatalog = { navController.navigate("categories") })
                }
                composable("categories") {
                    CategoryScreen(
                        onCategoryClick = { category -> navController.navigate("products/$category") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("products/{category}") { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    ProductListScreen(
                        category = category,
                        onProductClick = { productId -> navController.navigate("productDetail/$productId") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "productDetail/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                    ProductDetailScreen(
                        productId = productId,
                        onAddToCart = { cartViewModel.addToCart(it) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(BottomNavItem.Profile.route) { 
                    ProfileScreen(onLogout = {
                        navController.navigate("login") {
                            popUpTo("main_flow") { inclusive = true }
                        }
                    })
                }
                composable(BottomNavItem.Cart.route) { CartScreen(viewModel = cartViewModel) }
            }

            // Admin Flow
            navigation(startDestination = "admin_panel", route = "admin_flow/{username}") { 
                composable("admin_panel") { navBackStackEntry ->
                    val parentEntry = remember(navBackStackEntry) {
                        navController.getBackStackEntry("admin_flow/{username}")
                    }
                    val username = parentEntry.arguments?.getString("username") ?: ""
                    AdminPanelScreen(
                        username = username,
                        onAddProduct = { navController.navigate("admin_edit_product/-1") },
                        onEditProduct = { productId -> navController.navigate("admin_edit_product/$productId") },
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo("admin_flow") { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    route = "admin_edit_product/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getInt("productId")
                    val product = if (productId != null && productId != -1) adminViewModel.getProductById(productId) else null
                    ProductEditScreen(
                        product = product,
                        onSave = {
                            if (product == null) adminViewModel.addProduct(it) else adminViewModel.updateProduct(it)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}