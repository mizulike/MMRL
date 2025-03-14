package dev.dergoogler.mmrl.compat.impl

import android.os.ParcelFileDescriptor
import android.util.Base64
import android.util.Base64OutputStream
import dev.dergoogler.mmrl.compat.stub.IFileManager
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets


class FileManagerImpl : IFileManager.Stub() {

    init {
        System.loadLibrary("file-manager")
    }

    private external fun nativeList(path: String): Array<String>?
    private external fun nativeStat(path: String): Long
    private external fun nativeSize(path: String): Long
    private external fun nativeSizeRecursive(path: String): Long
    private external fun nativeExists(path: String): Boolean
    private external fun nativeIsDirectory(path: String): Boolean
    private external fun nativeIsFile(path: String): Boolean
    private external fun nativeMkdir(path: String): Boolean
    private external fun nativeMkdirs(path: String): Boolean
    private external fun nativeDelete(path: String): Boolean
    private external fun nativeWriteBytes(path: String, data: ByteArray): Boolean
    private external fun nativeReadByteBuffer(path: String): ByteBuffer?
    private external fun nativeRenameTo(srcPath: String, destPath: String): Boolean
    private external fun nativeCopyTo(
        srcPath: String,
        destPath: String,
        overwrite: Boolean,
    ): Boolean

    private external fun nativeSetOwner(path: String, owner: Int, group: Int): Boolean
    private external fun nativeSetPermissions(path: String, mode: Int): Boolean
    private external fun nativeCanExecute(path: String): Boolean
    private external fun nativeCanWrite(path: String): Boolean
    private external fun nativeCanRead(path: String): Boolean
    private external fun nativeIsHidden(path: String): Boolean
    private external fun nativeCreateNewFile(path: String): Boolean

    override fun deleteOnExit(path: String) = with(File(path)) {
        when {
            !exists() -> false
            isFile -> delete()
            isDirectory -> deleteRecursively()
            else -> false
        }
    }

    override fun writeBytes(path: String, data: ByteArray): Boolean {
        return nativeWriteBytes(path, data)
    }

    override fun writeText(path: String, data: String): Boolean =
        nativeWriteBytes(path, data.toByteArray())

    override fun readText(path: String): String {
        val buffer = nativeReadByteBuffer(path)
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    override fun readBytes(path: String): ByteArray? {
        val buffer: ByteBuffer = nativeReadByteBuffer(path) ?: return null
        return ByteArray(buffer.remaining()).apply { buffer.get(this) }
    }

    override fun readAsBase64(path: String): String? = with(File(path)) {
        if (!exists()) return null

        try {
            val `is`: InputStream = FileInputStream(this)
            val baos = ByteArrayOutputStream()
            val b64os = Base64OutputStream(baos, Base64.DEFAULT)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            try {
                while ((`is`.read(buffer).also { bytesRead = it }) > -1) {
                    b64os.write(buffer, 0, bytesRead)
                }
                return baos.toString()
            } catch (e: IOException) {
                Timber.e("FileManagerImpl>readAsBase64: $e")
                return null
            } finally {
                closeQuietly(`is`)
                closeQuietly(b64os)
            }
        } catch (e: FileNotFoundException) {
            Timber.e("FileManagerImpl>readAsBase64: $e")
            return null
        }
    }

    private fun closeQuietly(closeable: Closeable) {
        try {
            closeable.close()
        } catch (e: IOException) {
            Timber.e("FileManagerImpl>closeQuietly: $e")
        }
    }

    override fun list(path: String): Array<String>? = nativeList(path)
    override fun size(path: String): Long = nativeSize(path)
    override fun sizeRecursive(path: String): Long = nativeSizeRecursive(path)
    override fun stat(path: String): Long = nativeStat(path)
    override fun delete(path: String): Boolean = nativeDelete(path)
    override fun exists(path: String): Boolean = nativeExists(path)
    override fun isDirectory(path: String): Boolean = nativeIsDirectory(path)
    override fun isFile(path: String): Boolean = nativeIsFile(path)
    override fun mkdir(path: String): Boolean = nativeMkdir(path)
    override fun mkdirs(path: String): Boolean = nativeMkdirs(path)
    override fun createNewFile(path: String): Boolean = nativeCreateNewFile(path)
    override fun renameTo(target: String, dest: String): Boolean = nativeRenameTo(target, dest)
    override fun copyTo(
        target: String,
        dest: String,
        overwrite: Boolean,
    ): Boolean = nativeCopyTo(target, dest, overwrite)

    override fun canExecute(path: String): Boolean = nativeCanExecute(path)
    override fun canWrite(path: String): Boolean = nativeCanWrite(path)
    override fun canRead(path: String): Boolean = nativeCanRead(path)
    override fun isHidden(path: String): Boolean = nativeIsHidden(path)
    override fun setPermissions(path: String, mode: Int): Boolean =
        nativeSetPermissions(path, mode)

    override fun setOwner(path: String, owner: Int, group: Int): Boolean =
        nativeSetOwner(path, owner, group)

    private fun assertPath(path: String?) {
        if (path == null) {
            throw IllegalArgumentException("Path must be a string. Received null")
        }
    }

    override fun resolve(vararg paths: String): String {
        var resolvedPath = ""
        var resolvedAbsolute = false

        for (i in paths.indices.reversed()) {
            val path = paths[i]
            assertPath(path)

            if (path.isEmpty()) continue

            resolvedPath = "$path/$resolvedPath"
            resolvedAbsolute = path[0] == '/'

            if (resolvedAbsolute) break
        }

        resolvedPath = normalizeStringPosix(resolvedPath, !resolvedAbsolute)

        return when {
            resolvedAbsolute -> if (resolvedPath.isNotEmpty()) "/$resolvedPath" else "/"
            resolvedPath.isNotEmpty() -> resolvedPath
            else -> "."
        }
    }

    override fun normalizeStringPosix(path: String, allowAboveRoot: Boolean): String {
        var res = ""
        var lastSegmentLength = 0
        var lastSlash = -1
        var dots = 0
        var code: Char

        for (i in 0..path.length) {
            code = if (i < path.length) path[i] else '/'

            if (code == '/') {
                if (lastSlash == i - 1 || dots == 1) {
                    // NOOP
                } else if (lastSlash != i - 1 && dots == 2) {
                    if (res.length < 2 || lastSegmentLength != 2 || res.takeLast(2) != "..") {
                        if (res.length > 2) {
                            val lastSlashIndex = res.lastIndexOf('/')
                            if (lastSlashIndex != res.length - 1) {
                                res = if (lastSlashIndex == -1) "" else res.substring(
                                    0,
                                    lastSlashIndex
                                )
                                lastSegmentLength = res.length - 1 - res.lastIndexOf('/')
                                lastSlash = i
                                dots = 0
                                continue
                            }
                        } else if (res.length in 1..2) {
                            res = ""
                            lastSegmentLength = 0
                            lastSlash = i
                            dots = 0
                            continue
                        }
                    }
                    if (allowAboveRoot) {
                        res = if (res.isNotEmpty()) "$res/.." else ".."
                        lastSegmentLength = 2
                    }
                } else {
                    res = if (res.isNotEmpty()) "$res/${
                        path.substring(
                            lastSlash + 1,
                            i
                        )
                    }" else path.substring(lastSlash + 1, i)
                    lastSegmentLength = i - lastSlash - 1
                }
                lastSlash = i
                dots = 0
            } else if (code == '.' && dots != -1) {
                dots++
            } else {
                dots = -1
            }
        }
        return res
    }

    override fun parcelFile(filePath: String): ParcelFileDescriptor {
        return ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY)
    }
}