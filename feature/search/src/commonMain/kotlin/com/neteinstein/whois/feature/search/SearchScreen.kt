package com.neteinstein.whois.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neteinstein.whois.core.ui.components.WhoisPrimaryButton
import com.neteinstein.whois.core.ui.components.WhoisTextField
import com.neteinstein.whois.core.ui.components.WhoisTopBar
import com.neteinstein.whois.core.ui.strings.LocalStrings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    Scaffold(
        modifier = modifier,
        topBar = {
            WhoisTopBar(
                title = strings.searchTitle,
                settingsContentDescription = strings.settingsContentDescription,
                onSettingsClick = viewModel::onSettingsClicked,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = strings.searchSubtitle, style = MaterialTheme.typography.bodyMedium)

            AnimatedVisibility(
                visible = uiState.sharedContactBannerVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                SharedContactBanner(text = strings.sharedContactBanner)
            }

            WhoisTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = strings.fieldName,
            )
            WhoisTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = strings.fieldPhone,
            )
            WhoisTextField(
                value = uiState.company,
                onValueChange = viewModel::onCompanyChange,
                label = strings.fieldCompany,
            )
            WhoisTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                label = strings.fieldAddress,
            )

            AnimatedVisibility(
                visible = uiState.showEmptyFieldsError,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Text(
                    text = strings.searchEmptyError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            WhoisPrimaryButton(text = strings.searchButton, onClick = viewModel::onSearchClicked)
        }
    }
}

@Composable
private fun SharedContactBanner(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
