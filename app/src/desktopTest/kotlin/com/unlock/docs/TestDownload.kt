package com.unlock.docs

import java.net.HttpURLConnection
import java.net.URL

fun main() {
    val url =
        URL(
            "https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/Common-Credentials/xato-net-10-million-passwords-1000.txt",
        )
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    // connection.setRequestProperty("User-Agent", "Mozilla/5.0")
    connection.connect()

    println("Response Code: ${connection.responseCode}")
    println("Response Message: ${connection.responseMessage}")
}
