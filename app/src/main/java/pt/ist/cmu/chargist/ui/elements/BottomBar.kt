package pt.ist.cmu.chargist.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor

@Composable
fun BottomNavigationBar(
    onHomeClick : () -> Unit = {},
    onSearchClick : () -> Unit = {},
    onAccountClick : () -> Unit = {},
    currentScreen : String
) {
    Column {
        Divider(
            color = mainColor,
            thickness = 2.dp
        )
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val texts = listOf<String>("Map", "Search", "Account")
                val icons = listOf<ImageVector>(Icons.Default.Map, Icons.Default.Search, Icons.Default.AccountCircle)
                val callbacks = listOf<()->Unit>(onHomeClick, onSearchClick, onAccountClick)

                for (i in 0..2) {
                    CustomButton (
                        texts[i],
                        icons[i],
                        callbacks[i],
                        currentScreen == texts[i]
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomButton(text : String, icon : ImageVector, onClick : () -> Unit, inThisScreen : Boolean) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier.size(96.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = text, tint = if (inThisScreen) mainColor else LocalContentColor.current)
            Text(text = text, color = if (inThisScreen) mainColor else LocalContentColor.current)
        }
    }
}