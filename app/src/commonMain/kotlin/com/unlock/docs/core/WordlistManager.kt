package com.unlock.docs.core

import kotlinx.coroutines.flow.Flow

data class CuratedWordlist(
    val name: String,
    val description: String,
    val url: String,
    val expectedSizeMb: Float,
)

val DefaultWordlists =
    listOf(
        CuratedWordlist(
            "Top 1000",
            "The 1,000 most common passwords",
            "https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/Common-Credentials/xato-net-10-million-passwords-1000.txt",
            0.01f,
        ),
        CuratedWordlist(
            "Top 10,000",
            "The 10,000 most common passwords",
            "https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/Common-Credentials/xato-net-10-million-passwords-10000.txt",
            0.1f,
        ),
        CuratedWordlist(
            "Top 1,000,000",
            "The 1,000,000 most common passwords",
            "https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/Common-Credentials/xato-net-10-million-passwords-1000000.txt",
            8.5f,
        ),
    )

expect object WordlistDownloader {
    /**
     * Downloads a wordlist to local storage and returns the absolute path.
     * Emits progress from 0.0f to 1.0f.
     */
    fun downloadWordlist(wordlist: CuratedWordlist): Flow<Float>

    /**
     * Checks if the wordlist is already downloaded locally.
     */
    fun isDownloaded(wordlist: CuratedWordlist): Boolean

    /**
     * Gets the local file path for a downloaded wordlist.
     */
    fun getLocalPath(wordlist: CuratedWordlist): String?
}
