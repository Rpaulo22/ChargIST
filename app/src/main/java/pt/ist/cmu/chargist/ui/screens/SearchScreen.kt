package pt.ist.cmu.chargist.ui.screens

import android.R.attr.text
import android.R.attr.thickness
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ist.cmu.chargist.Screen
import pt.ist.cmu.chargist.ui.elements.BottomNavigationBar
import pt.ist.cmu.chargist.viewmodel.AccountViewModel
import pt.ist.cmu.chargist.viewmodel.AppViewModel

@Composable
fun SearchScreen(
    onAccountClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    SearchScreenContent(
        goToHomeScreen = onHomeClick,
        goToAccountScreen = onAccountClick,
    )
}

@Composable
private fun SearchScreenContent (
    goToAccountScreen: () -> Unit,
    goToHomeScreen: () -> Unit,
) {
    Scaffold (
        bottomBar = {
            BottomNavigationBar(
                onAccountClick = goToAccountScreen,
                onHomeClick = goToHomeScreen,
                currentScreen = "Search"
            )
        }
    ) { paddingValues ->
        Text("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA (This be da Search screen btw, mon)", Modifier.padding(paddingValues))
    }
}