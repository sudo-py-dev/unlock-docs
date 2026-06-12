package com.unlock.docs.core

expect object SessionManager {
    fun saveSession(filePath: String, mode: String, offset: Long)
    fun getSession(filePath: String): Long?
    fun clearSession(filePath: String)
}
