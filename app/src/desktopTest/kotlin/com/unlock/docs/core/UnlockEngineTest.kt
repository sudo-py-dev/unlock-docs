package com.unlock.docs.core

import com.unlock.docs.core.format.OfficeHandlerImpl
import com.unlock.docs.core.format.ZipHandlerImpl
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.crypt.EncryptionMode
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnlockEngineTest {
    private lateinit var tempZipFile: File
    private lateinit var tempOfficeFile: File

    private val testPassword = "testPassword123"

    @Before
    fun setup() {
        // Create an encrypted ZIP file
        val tempDir = File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString())
        tempDir.mkdirs()

        val dummyTextFile = File(tempDir, "dummy.txt")
        dummyTextFile.writeText("This is a test file to be encrypted")

        tempZipFile = File(tempDir, "test_encrypted.zip")
        val zipParams = ZipParameters()
        zipParams.isEncryptFiles = true
        zipParams.encryptionMethod = EncryptionMethod.AES
        zipParams.aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256

        val zipFile = ZipFile(tempZipFile, testPassword.toCharArray())
        zipFile.addFile(dummyTextFile, zipParams)

        // Create an encrypted Office file
        tempOfficeFile = File(tempDir, "test_encrypted.xlsx")
        val fs = POIFSFileSystem()
        val info = EncryptionInfo(EncryptionMode.agile)
        val enc = info.encryptor
        enc.confirmPassword(testPassword)

        // Create a dummy workbook
        val wb = XSSFWorkbook()
        wb.createSheet("Test Sheet")
        val fileOut = File(tempDir, "dummy.xlsx")
        FileOutputStream(fileOut).use { wb.write(it) }
        wb.close()

        // Encrypt the workbook
        val opcStream = enc.getDataStream(fs)
        val tempIn = fileOut.inputStream()
        tempIn.copyTo(opcStream)
        tempIn.close()
        opcStream.close()

        FileOutputStream(tempOfficeFile).use { fs.writeFilesystem(it) }
        fs.close()
    }

    @After
    fun teardown() {
        tempZipFile.parentFile.deleteRecursively()
    }

    @Test
    fun testZipArchiveUnlockSuccess() =
        runBlocking {
            val passwords = listOf("wrong1", "wrong2", testPassword, "wrong3").asFlow()
            val engine = UnlockEngine(ZipHandlerImpl(), tempZipFile.absolutePath)

            val found = engine.unlock(passwords, concurrency = 4)
            assertEquals(testPassword, found)
        }

    @Test
    fun testOfficeDocumentUnlockSuccess() =
        runBlocking {
            val passwords = listOf("wrong1", testPassword, "wrong2").asFlow()
            val engine = UnlockEngine(OfficeHandlerImpl(), tempOfficeFile.absolutePath)

            val found = engine.unlock(passwords, concurrency = 2)
            assertEquals(testPassword, found)
        }

    @Test
    fun testArchiveUnlockFailureReturnsNullSafelyWithoutRaceConditions() =
        runBlocking {
            // Feed a large sequence of incorrect passwords to heavily stress multi-threading logic
            val passwords = (1..1000).map { "wrong_$it" }.asFlow()
            val engine = UnlockEngine(ZipHandlerImpl(), tempZipFile.absolutePath)

            val found = engine.unlock(passwords, concurrency = 8)
            assertNull(found)
        }
}
