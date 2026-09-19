package com.neteinstein.whois.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neteinstein.whois.core.common.AppLanguage
import com.neteinstein.whois.core.ui.strings.LocalStrings
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val strings = LocalStrings.current
    val selectedLanguage by viewModel.language.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = strings.languageSectionTitle, style = MaterialTheme.typography.titleMedium)
                LanguageSelector(
                    selected = selectedLanguage,
                    labelFor = { if (it == AppLanguage.ENGLISH) strings.languageEnglish else strings.languagePortuguese },
                    onSelected = viewModel::onLanguageSelected,
                )
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = strings.aboutSectionTitle, style = MaterialTheme.typography.titleMedium)
                Text(text = strings.aboutDescription, style = MaterialTheme.typography.bodyMedium)
                Text(text = strings.aboutAuthor, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = viewModel::onViewSourceClicked) {
                    Text(strings.aboutViewSource)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    selected: AppLanguage,
    labelFor: (AppLanguage) -> String,
    onSelected: (AppLanguage) -> Unit,
) {
    val languages = AppLanguage.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        languages.forEachIndexed { index, language ->
            SegmentedButton(
                selected = selected == language,
                onClick = { onSelected(language) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = languages.size),
            ) {
                Text(labelFor(language))
            }
        }
    }
}
