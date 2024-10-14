package com.pav_analytics.FileManager

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun zipFiles(files: List<File>, outputFile: File): File {
    ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
        files.forEach { file ->
            FileInputStream(file).use { fis ->
                val zipEntry = ZipEntry(file.name)
                zos.putNextEntry(zipEntry)

                val buffer = ByteArray(1024)
                var length: Int

                while (fis.read(buffer).also { length = it } >= 0) {
                    zos.write(buffer, 0, length)
                }

                zos.closeEntry()
            }
        }
    }
    return outputFile
}