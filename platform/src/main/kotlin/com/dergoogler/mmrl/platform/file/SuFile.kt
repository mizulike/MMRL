package com.dergoogler.mmrl.platform.file

import android.os.ParcelFileDescriptor
import android.os.RemoteException
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.stub.IFileManager
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * `SuFile` is a wrapper class around `java.io.File` that provides enhanced file system operations,
 * including support for managing files and directories through an `IFileManager` interface.
 * It facilitates platform-specific file management, offering a consistent API across different
 * environments.
 *
 * This class provides a range of functions for interacting with files and directories, such as reading,
 * writing, creating, deleting, and managing permissions. It also introduces functionalities
 * for working with streams and handling exceptions.
 *
 * @param path The path to the file or directory represented by this `SuFile` instance.
 *
 * @property fileManager The `IFileManager` instance used to perform file system operations. Defaults to
 *                       `Platform.fileManager`.
 */
class SuFile(
    path: String,
) : File(path) {
    var fileManager: IFileManager = Platform.fileManager

    constructor(
        path: String,
        parent: SuFile,
    ) : this(SuFile(parent, path).path)

    constructor(vararg paths: String) : this(resolve(*paths))

    constructor(vararg paths: Any) : this(
        resolve(*paths.map {
            when (it) {
                is File -> it.path
                is String -> it
                else -> throw IllegalArgumentException("Unsupported type: ${it::class}")
            }
        }.toTypedArray())
    )

    fun readText(): String {
        val bytes = newInputStream().use { it.readBytes() }
        val data = ByteBuffer.wrap(bytes)
        val content = StandardCharsets.UTF_8.decode(data).toString()

        return content
    }

    fun fromPaths(vararg paths: Any): SuFile? {
        val files = paths.map { SuFile(path, it) }
        for (f in files) {
            if (f.exists()){
                return f
            }
        }

        return null
    }

    fun parcelStream(): FileInputStream {
        val fd = fileManager.parcelFile(this.path)
        return FileInputStream(fd.fileDescriptor)
    }

    fun readBytes(): ByteArray = newInputStream().use { it.readBytes() }

    fun writeText(data: String) = newOutputStream(false).use { it.write(data.toByteArray()) }

    fun writeBytes(data: ByteArray) = newOutputStream(false).use { it.write(data) }

    override fun list(): Array<String> {
        return fileManager.list(this.path)
    }

    override fun length(): Long {
        return this.size(false)
    }

    fun size(recursively: Boolean = false): Long = fileManager.size(this.path, recursively)

    fun stat(): Long {
        return this.lastModified()
    }

    override fun lastModified(): Long {
        return fileManager.stat(this.path)
    }

    override fun exists(): Boolean {
        return fileManager.exists(this.path)
    }

    fun exists(block: (SuFile) -> Unit) {
        if (exists()) block(this)
    }

    override fun isDirectory(): Boolean {
        return fileManager.isDirectory(this.path)
    }

    override fun isFile(): Boolean {
        return fileManager.isFile(this.path)
    }

    fun isBlock(): Boolean = fileManager.isBlock(this.path)

    fun isCharacter(): Boolean = fileManager.isCharacter(this.path)

    fun isSymlink(): Boolean = fileManager.isSymlink(this.path)

    fun isNamedPipe(): Boolean = fileManager.isNamedPipe(this.path)

    fun isSocket(): Boolean = fileManager.isSocket(this.path)

    override fun mkdir(): Boolean {
        return fileManager.mkdir(this.path)
    }

    override fun mkdirs(): Boolean {
        return fileManager.mkdirs(this.path)
    }

    override fun createNewFile(): Boolean {
        return fileManager.createNewFile(this.path)
    }

    override fun renameTo(dest: File): Boolean {
        return fileManager.renameTo(this.path, dest.path)
    }

    fun copyTo(dest: File, overwrite: Boolean = false) {
        return fileManager.copyTo(this.path, dest.path, overwrite)
    }

    override fun canExecute(): Boolean {
        return fileManager.canExecute(this.path)
    }

    override fun canRead(): Boolean {
        return fileManager.canRead(this.path)
    }

    override fun canWrite(): Boolean {
        return fileManager.canWrite(this.path)
    }

    override fun delete(): Boolean {
        return fileManager.delete(this.path)
    }

    override fun deleteOnExit() {
        fileManager.deleteOnExit(this.path)
    }

    override fun isHidden(): Boolean {
        return fileManager.isHidden(this.path)
    }

    override fun setReadOnly(): Boolean {
        return setPermissions(
            SuFilePermissions.combine(
                SuFilePermissions.OWNER_READ,
                SuFilePermissions.GROUP_READ,
                SuFilePermissions.OTHERS_READ,
            )
        )
    }

    override fun setExecutable(executable: Boolean): Boolean {
        return setPermissions(SuFilePermissions.PERMISSION_755)
    }

    fun setPermissions(permissions: SuFilePermissions): Boolean {
        return fileManager.setPermissions(this.path, permissions.value)
    }

    fun setPermissions(permissions: Int): Boolean {
        return fileManager.setPermissions(this.path, permissions)
    }

    fun setOwner(uid: Int, gid: Int): Boolean {
        return fileManager.setOwner(this.path, uid, gid)
    }

    override fun listFiles(): Array<SuFile> {
        return this.list().map { SuFile(it, this) }.toTypedArray()
    }

    override fun listFiles(filter: FileFilter?): Array<SuFile> {
        val ss = list()
        val files = ArrayList<SuFile>()
        for (s in ss) {
            val f = SuFile(s, this)
            if ((filter == null) || filter.accept(f)) files.add(f)
        }
        return files.toArray(arrayOfNulls<SuFile>(files.size))
    }


    @Throws(IOException::class)
    fun newInputStream(): InputStream {
        val pipe = ParcelFileDescriptor.createPipe()
        try {
            fileManager.openReadStream(this.path, pipe[1]).checkException()
        } catch (e: RemoteException) {
            pipe[0].close()
            throw IOException(e)
        } finally {
            pipe[1].close()
        }
        return ParcelFileDescriptor.AutoCloseInputStream(pipe[0])
    }

    @Throws(IOException::class)
    fun newOutputStream(append: Boolean): OutputStream {
        val pipe = ParcelFileDescriptor.createPipe()
        try {
            fileManager.openWriteStream(this.path, pipe[0], append).checkException()
        } catch (e: RemoteException) {
            pipe[1].close()
            throw IOException(e)
        } finally {
            pipe[0].close()
        }
        return ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
    }

    @Throws(IOException::class)
    fun getCanonicalDirPath(): String {
        var canonicalPath = this.canonicalPath
        if (!canonicalPath.endsWith("/")) canonicalPath += "/"
        return canonicalPath
    }

    @Throws(IOException::class)
    fun getCanonicalFileIfChild(child: String): SuFile? {
        val parentCanonicalPath = getCanonicalDirPath()
        val childCanonicalPath = SuFile(this, child).canonicalPath
        if (childCanonicalPath.startsWith(parentCanonicalPath)) {
            return SuFile(childCanonicalPath)
        }
        return null
    }


    companion object {
        const val PIPE_CAPACITY = 16 * 4096

        fun String.toSuFile(): SuFile {
            return SuFile(this)
        }

        fun createDirectories(vararg file: SuFile): Boolean {
            for (f in file) {
                if (!f.mkdirs()) return false
            }
            return true
        }

        private fun assertPath(path: String?) {
            if (path == null) {
                throw IllegalArgumentException("Path must be a string. Received null")
            }
        }

        fun resolve(vararg paths: String): String {
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

        fun normalizeStringPosix(path: String, allowAboveRoot: Boolean): String {
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

        fun Int.toFormattedFileSize(): String = toDouble().toFormattedFileSize()

        fun Long.toFormattedFileSize(): String = toDouble().toFormattedFileSize()

        fun Float.toFormattedFileSize(): String = toDouble().toFormattedFileSize()

        fun Double.toFormattedFileSize(): String {
            if (this < 1024) return "$this B"

            val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
            var size = this
            val sizeRepresentation = SuFileSizeRepresentation.getRepresentation(size)
            val base = sizeRepresentation.base.toDouble()
            var unitIndex = 0

            while (size >= base && unitIndex < units.size - 1) {
                size /= base
                unitIndex++
            }

            return if (size == size.toLong().toDouble()) {
                String.format(Locale.getDefault(), "%.0f %s", size, units[unitIndex])
            } else {
                String.format(Locale.getDefault(), "%.2f %s", size, units[unitIndex])
            }
        }
    }
}