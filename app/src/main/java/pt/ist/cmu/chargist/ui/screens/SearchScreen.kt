package pt.ist.cmu.chargist.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import pt.ist.cmu.chargist.ui.elements.BottomNavigationBar
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor

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

    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val onSearch = { input:String -> /*TODO*/ }
    val onSort = { showSortDialog = true }
    val onFilter = { showFilterDialog = true }

    var sortBy: String? = null
    var filterSpeed: String? = null
    var filterDistanceMin: Double? = null
    var filterDistanceMax: Double? = null
    var filterPriceMin: Double? = null
    var filterPriceMax: Double? = null
    var filterTravelTimeMin: Double? = null
    var filterTravelTimeMax: Double? = null

    Scaffold (
        bottomBar = {
            BottomNavigationBar(
                onAccountClick = goToAccountScreen,
                onHomeClick = goToHomeScreen,
                currentScreen = "Search"
            )
        }
    ) { paddingValues ->
        Box(Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
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

        if (showSortDialog) {
            SortDialog(
                { showSortDialog = false },
                { s: String? -> sortBy = s},
                sortBy
            )
        }

        if (showFilterDialog) {
            FilterDialog(
                { showFilterDialog = false },
                { s: String?, dMin: Double?, dMax: Double?, pMin: Double?, pMax: Double?, tMin: Double?, tMax: Double? ->
                    filterSpeed = s
                    filterDistanceMin = dMin
                    filterDistanceMax = dMax
                    filterPriceMin = pMin
                    filterPriceMax = pMax
                    filterTravelTimeMin = tMin
                    filterTravelTimeMax = tMax
                },
                filterSpeed,
                filterDistanceMin,
                filterDistanceMax,
                filterPriceMin,
                filterPriceMax,
                filterTravelTimeMin,
                filterTravelTimeMax
            )
        }
    }
}

@Composable
fun SortDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
    prevSelection: String?
) {
    var selectedOption: String? by remember { mutableStateOf(prevSelection) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort by") },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SortOptionsDropdown(
    { s ->
                        selectedOption = s
                    },
                    {
                        onConfirm(selectedOption)
                        onDismiss()
                    },
                    selectedOption
                )
            }
        },
        confirmButton = { },
        dismissButton = { }
    )
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?, Double?, Double?, Double?, Double?, Double?, Double?) -> Unit,
    prevFilterSpeed: String?,
    prevFilterDistanceMin: Double?,
    prevFilterDistanceMax: Double?,
    prevFilterPriceMin: Double?,
    prevFilterPriceMax: Double?,
    prevFilterTravelTimeMin: Double?,
    prevFilterTravelTimeMax: Double?
) {
    var selectedSpeed = prevFilterSpeed
    var selectedDistanceMin = prevFilterDistanceMin
    var selectedDistanceMax = prevFilterDistanceMax
    var selectedPriceMin = prevFilterPriceMin
    var selectedPriceMax = prevFilterPriceMax
    var selectedTravelTimeMin = prevFilterTravelTimeMin
    var selectedTravelTimeMax = prevFilterTravelTimeMax

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by") },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvailabilityFilter()
                Spacer(Modifier.size(8.dp))
                HorizontalDivider()
                Spacer(Modifier.size(8.dp))
                SpeedFilter({ s:String -> selectedSpeed = s}, selectedSpeed)
                Spacer(Modifier.size(8.dp))
                HorizontalDivider()
                Spacer(Modifier.size(8.dp))
                DistanceFilter(
                    { min ->
                        selectedDistanceMin = min
                    },
                    { max ->
                        selectedDistanceMax = max
                    }, selectedDistanceMin, selectedDistanceMax)
                Spacer(Modifier.size(8.dp))
                HorizontalDivider()
                Spacer(Modifier.size(8.dp))
                PriceFilter({ min ->
                        selectedPriceMin = min
                    },
                    { max ->
                        selectedPriceMax = max
                    }, selectedPriceMin, selectedPriceMax)
                Spacer(Modifier.size(8.dp))
                HorizontalDivider()
                Spacer(Modifier.size(8.dp))
                TravelTimeFilter({ min ->
                        selectedTravelTimeMin = min
                    },
                    { max ->
                        selectedTravelTimeMax = max
                    }, selectedTravelTimeMin, selectedTravelTimeMax)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedSpeed, selectedDistanceMin, selectedDistanceMax, selectedPriceMin, selectedPriceMax, selectedTravelTimeMin, selectedTravelTimeMax)
                onDismiss()
            }) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SortOptionsDropdown(onOptionChange: (String) -> Unit, onComplete: () -> Unit, savedText: String? = null) {
    var mExpanded by remember { mutableStateOf(true) }

    val mOptions = listOf("Charging Speed", "Distance", "Price", "Travel Time", "Availability") /*TODO: pôr a Availability pela ordem alfabética caso ela chegue a ser implementada*/
    var mSelectedText by remember { mutableStateOf(savedText?:mOptions[0]) }
    var mTextFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero)}

    onOptionChange(mSelectedText) // This is redundant every time except the first when there is no previous option

    // Up Icon when expanded and down icon when collapsed
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(Modifier.padding(20.dp)) {
        OutlinedTextField(
            value = mSelectedText,
            onValueChange = { mSelectedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    mTextFieldSize = coordinates.size.toSize()
                }
                .clickable(onClick = { mExpanded = !mExpanded }),
            enabled = false,
            trailingIcon = {
                Icon(icon,"contentDescription",
                    Modifier.clickable { mExpanded = !mExpanded })
            },
            readOnly = true,
            colors = TextFieldDefaults.colors(
                disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor,
                disabledLabelColor = TextFieldDefaults.colors().unfocusedLabelColor
            )
        )

        DropdownMenu(
            expanded = mExpanded,
            onDismissRequest = { mExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current){mTextFieldSize.width.toDp()})
        ) {
            mOptions.forEach { label ->
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        mSelectedText = label
                        onOptionChange(mSelectedText)
                        mExpanded = false
                    }
                )
            }

            if (!mExpanded)
                onComplete()
        }
    }
}

@Composable
fun SpeedFilter(onSelected: (String) -> Unit, selectedSpeed: String?) {
    val speedOptions = listOf("≥ Slow", "≥ Medium", "≥ Fast")
    val (selectedSpeed, onSpeedSelected) = remember { mutableStateOf(selectedSpeed?:speedOptions[0]) }
    onSelected(selectedSpeed) // Useful the first time only

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Charging speed",
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        Row(
            modifier = Modifier.selectableGroup().fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            speedOptions.forEach { speed ->
                Column(
                    modifier = Modifier
                        .selectable(
                            selected = (speed == selectedSpeed),
                            onClick = { onSpeedSelected(speed); onSelected(speed) },
                            role = Role.RadioButton
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = speed
                    )
                    RadioButton(
                        selected = (speed == selectedSpeed),
                        onClick = null
                    )
                }
            }
        }
    }
}

@Composable
fun DistanceFilter (
    onMinChange: (Double)->Unit,
    onMaxChange: (Double)->Unit,
    savedMin: Double?,
    savedMax:Double?
) {
    val defaultMin = "0.0"
    val defaultMax = "100.0"
    var minDistanceInput by remember { mutableStateOf(savedMin?.toString() ?: defaultMin) }
    var maxDistanceInput by remember { mutableStateOf(savedMax?.toString() ?: defaultMax) }
    Column {
        Text(
            "Distance",
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Row {
            TextField(
                value = minDistanceInput,
                onValueChange = {
                    minDistanceInput = it.replace(",", ".")
                    val v = minDistanceInput.toDoubleOrNull()
                    if (v != null && v < defaultMin.toDouble())
                        minDistanceInput = defaultMin
                    onMinChange(v ?: defaultMin.toDouble())
                },
                label = { Text("Min") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("km") }
            )
        }
        Spacer(Modifier.size(4.dp))
        Row {
            TextField(
                value = maxDistanceInput,
                onValueChange = {
                    maxDistanceInput = it.replace(",",".")
                    val v = maxDistanceInput.toDoubleOrNull()
                    if (v != null && v > defaultMax.toDouble())
                        maxDistanceInput = defaultMax
                    onMaxChange(v?:defaultMax.toDouble())
                },
                label = { Text("Max") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("km")}
            )
        }
    }
}

@Composable
fun PriceFilter (
    onMinChange: (Double)->Unit,
    onMaxChange: (Double)->Unit,
    savedMin: Double?,
    savedMax:Double?
) {
    val defaultMin = "0.0"
    val defaultMax = "100.0"
    var minDistanceInput by remember { mutableStateOf(savedMin?.toString() ?: defaultMin) }
    var maxDistanceInput by remember { mutableStateOf(savedMax?.toString() ?: defaultMax) }
    Column {
        Text(
            "Price",
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Row {
            TextField(
                value = minDistanceInput,
                onValueChange = {
                    minDistanceInput = it.replace(",", ".")
                    val v = minDistanceInput.toDoubleOrNull()
                    if (v != null && v < defaultMin.toDouble())
                        minDistanceInput = defaultMin
                    onMinChange(v ?: defaultMin.toDouble())
                },
                label = { Text("Min") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("€/kWh") }
            )
        }
        Spacer(Modifier.size(4.dp))
        Row {
            TextField(
                value = maxDistanceInput,
                onValueChange = {
                    maxDistanceInput = it.replace(",",".")
                    val v = maxDistanceInput.toDoubleOrNull()
                    if (v != null && v > defaultMax.toDouble())
                        maxDistanceInput = defaultMax
                    onMaxChange(v?:defaultMax.toDouble())
                },
                label = { Text("Max") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("€/kWh")}
            )
        }
    }
}


@Composable
fun AvailabilityFilter() {
    // TODO:
    Column {

    }
}

@Composable
fun TravelTimeFilter (
    onMinChange: (Double)->Unit,
    onMaxChange: (Double)->Unit,
    savedMin: Double?,
    savedMax:Double?
) {
    val defaultMin = "0.0"
    val defaultMax = "500.0"
    var minDistanceInput by remember { mutableStateOf(savedMin?.toString() ?: defaultMin) }
    var maxDistanceInput by remember { mutableStateOf(savedMax?.toString() ?: defaultMax) }
    Column {
        Text(
            "Travel Time",
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Row {
            TextField(
                value = minDistanceInput,
                onValueChange = {
                    minDistanceInput = it.replace(",", ".")
                    val v = minDistanceInput.toDoubleOrNull()
                    if (v != null && v < defaultMin.toDouble())
                        minDistanceInput = defaultMin
                    onMinChange(v ?: defaultMin.toDouble())
                },
                label = { Text("Min") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("min") }
            )
        }
        Spacer(Modifier.size(4.dp))
        Row {
            TextField(
                value = maxDistanceInput,
                onValueChange = {
                    maxDistanceInput = it.replace(",",".")
                    val v = maxDistanceInput.toDoubleOrNull()
                    if (v != null && v > defaultMax.toDouble())
                        maxDistanceInput = defaultMax
                    onMaxChange(v?:defaultMax.toDouble())
                },
                label = { Text("Max") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("min")}
            )
        }
    }
}

@Preview
@Composable
fun SearchScreenPreview() {
    SearchScreen({}, {})
}