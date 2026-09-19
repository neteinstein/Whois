package com.neteinstein.whois.core.ui.strings

/**
 * All user-facing copy, in-memory rather than platform resource files, so switching the
 * in-app language (Settings > Language) updates the UI instantly on both Android and iOS
 * without an activity/app restart or per-platform resource-locale plumbing.
 */
data class Strings(
    val appName: String,
    val splashTagline: String,
    val searchTitle: String,
    val searchSubtitle: String,
    val fieldName: String,
    val fieldPhone: String,
    val fieldCompany: String,
    val fieldAddress: String,
    val searchButton: String,
    val searchEmptyError: String,
    val sharedContactBanner: String,
    val settingsTitle: String,
    val settingsContentDescription: String,
    val languageSectionTitle: String,
    val languageEnglish: String,
    val languagePortuguese: String,
    val aboutSectionTitle: String,
    val aboutDescription: String,
    val aboutAuthor: String,
    val aboutViewSource: String,
    val aboutVersion: String,
)

val EnglishStrings = Strings(
    appName = "Whois",
    splashTagline = "Find out who's behind it",
    searchTitle = "Whois",
    searchSubtitle = "Enter what you know, we'll ask the web",
    fieldName = "Person or company name",
    fieldPhone = "Phone number",
    fieldCompany = "Company",
    fieldAddress = "Address",
    searchButton = "Search",
    searchEmptyError = "Enter at least one field to search",
    sharedContactBanner = "Loaded from shared contact",
    settingsTitle = "Settings",
    settingsContentDescription = "Open settings",
    languageSectionTitle = "Language",
    languageEnglish = "English",
    languagePortuguese = "Português",
    aboutSectionTitle = "About",
    aboutDescription = "Whois helps you quickly search the public web for information about a " +
        "person or company using whatever details you have on hand.",
    aboutAuthor = "By neteinstein",
    aboutViewSource = "View source on GitHub",
    aboutVersion = "Version",
)

val PortugueseStrings = Strings(
    appName = "Whois",
    splashTagline = "Descubra quem está por trás",
    searchTitle = "Whois",
    searchSubtitle = "Digite o que você sabe, nós perguntamos à web",
    fieldName = "Nome da pessoa ou empresa",
    fieldPhone = "Número de telefone",
    fieldCompany = "Empresa",
    fieldAddress = "Endereço",
    searchButton = "Pesquisar",
    searchEmptyError = "Preencha pelo menos um campo para pesquisar",
    sharedContactBanner = "Carregado a partir de um contato compartilhado",
    settingsTitle = "Configurações",
    settingsContentDescription = "Abrir configurações",
    languageSectionTitle = "Idioma",
    languageEnglish = "English",
    languagePortuguese = "Português",
    aboutSectionTitle = "Sobre",
    aboutDescription = "O Whois ajuda você a pesquisar rapidamente na web pública informações " +
        "sobre uma pessoa ou empresa usando os dados que você tiver em mãos.",
    aboutAuthor = "Por neteinstein",
    aboutViewSource = "Ver código-fonte no GitHub",
    aboutVersion = "Versão",
)
