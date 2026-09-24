package dev.jparizek.adaptivenavsuite.android.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import dev.jparizek.adaptivenavsuite.AppDestination

/**
 * Maps the shared, framework-agnostic icon keys from [AppDestination] to Compose `ImageVector`s.
 * This mapping is the only Android-specific piece of icon knowledge in the app — the shared
 * module never depends on Compose.
 */
private val outlineIcons: Map<String, ImageVector> = mapOf(
    "home_outline" to Icons.Outlined.Home,
    "favorite_outline" to Icons.Outlined.FavoriteBorder,
    "shopping_cart_outline" to Icons.Outlined.ShoppingCart,
    "account_circle_outline" to Icons.Outlined.AccountCircle,
)

private val filledIcons: Map<String, ImageVector> = mapOf(
    "home_filled" to Icons.Filled.Home,
    "favorite_filled" to Icons.Filled.Favorite,
    "shopping_cart_filled" to Icons.Filled.ShoppingCart,
    "account_circle_filled" to Icons.Filled.AccountCircle,
)

fun AppDestination.icon(selected: Boolean): ImageVector {
    val key = if (selected) materialSelectedIconName else materialIconName
    return (if (selected) filledIcons[key] else outlineIcons[key])
        ?: error("No ImageVector mapped for icon key '$key'. Add it to DestinationIcons.kt.")
}
