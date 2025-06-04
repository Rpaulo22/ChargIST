package pt.ist.cmu.chargist.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onAccountClick : () -> Unit = {}
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
                CustomButton(
                    "Map",
                    Icons.Default.Map,
                    onHomeClick
                )

                CustomButton(
                    "Search",
                    Icons.Default.Search,
                    onSearchClick
                )

                CustomButton(
                    "Account",
                    Icons.Default.AccountCircle,
                    onAccountClick
                )
            }
        }
    }
}

@Composable
private fun CustomButton(text : String, icon : ImageVector, onClick : () -> Unit) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier.size(96.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = text)
            Text(text = text)
        }
    }
}