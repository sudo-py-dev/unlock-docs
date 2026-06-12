package com.unlock.docs.core.format

interface ArchiveHandler {
    /**
     * Prepares the handler for the given file.
     * Returns true if the file is valid and requires a password.
     */
    fun initialize(filePath: String): Boolean

    /**
     * Checks a specific password. Must be thread-safe or fast enough.
     */
    fun checkPassword(password: String): Boolean
}

object HandlerRegistry {
    var zipHandler: ArchiveHandler? = null
    var officeHandler: ArchiveHandler? = null
}
