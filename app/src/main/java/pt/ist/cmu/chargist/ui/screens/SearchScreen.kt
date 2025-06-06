package pt.ist.cmu.chargist.ui.screens

import android.R.attr.onClick
import android.R.attr.text
import android.R.attr.thickness
import android.R.id.input
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ist.cmu.chargist.Screen
import pt.ist.cmu.chargist.ui.elements.BottomNavigationBar
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor
import pt.ist.cmu.chargist.viewmodel.AccountViewModel
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import kotlin.math.exp

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenContent (
    goToAccountScreen: () -> Unit,
    goToHomeScreen: () -> Unit,
) {
    var textFieldState = remember { TextFieldState() }
    var searchResults = remember { listOf<String>() }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val onSearch = { input:String -> /*TODO*/ }
    val onSort = {}
    val onFilter = {}

    Scaffold (
        bottomBar = {
            BottomNavigationBar(
                onAccountClick = goToAccountScreen,
                onHomeClick = goToHomeScreen,
                currentScreen = "Search"
            )
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            Column(Modifier.align(Alignment.TopCenter)) {
                SearchBar(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = textFieldState.text.toString(),
                            onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                            onSearch = {
                                onSearch(textFieldState.text.toString())
                                expanded = false
                            },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            placeholder = { Text("Search remote location") }
                        )
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    Column(
                        Modifier.verticalScroll(rememberScrollState())
                    ) {
                        searchResults.forEach { result ->
                            /*TODO*/
                        }
                    }
                }
                Spacer(Modifier.size(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onSort,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(30),
                        border = BorderStroke(2.dp, mainColor),
                        contentPadding = PaddingValues(horizontal = 56.dp, vertical = 8.dp),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Sort Search",
                            modifier = Modifier.size(24.dp),
                            tint = mainColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sort", color = mainColor, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onFilter,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(30),
                        border = BorderStroke(2.dp, mainColor),
                        contentPadding = PaddingValues(horizontal = 56.dp, vertical = 8.dp),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "Filter Search",
                            modifier = Modifier.size(24.dp),
                            tint = mainColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Filter", color = mainColor, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchScreenPreview() {
    SearchScreen({}, {})
}