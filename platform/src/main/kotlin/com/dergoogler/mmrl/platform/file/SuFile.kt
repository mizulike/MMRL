package com.dergoogler.mmrl.platform.file

import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.LruCache
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.stub.IFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class SuFile(
    path: String,
) : File(path) {
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
                is Uri -> it.toString()
                else -> throw IllegalArgumentException("Unsupported type: ${it::class}")
            }
        }.toTypedArray())
    )

    private val mCache: LruCache<String, File> = object : LruCache<String, File>(100) {
        override fun create(key: String): File {
            return File(key)
        }
    }

    private fun <R> fallback(
        root: IFileManager.() -> R,
        nonRoot: File.() -> R,
    ): R {
        val fileManager = Platform.fileManagerOrNull
        try {
            if (fileManager != null) {
                return root(fileManager)
            }
            return nonRoot(mCache.get(this.path))
        } catch (e: Exception) {
            return nonRoot(mCache.get(this.path))
        }
    }

    fun readText(): String {
        val bytes = newInputStream().use { it.readBytes() }
        val data = ByteBuffer.wrap(bytes)
        val content = StandardCharsets.UTF_8.decode(data).toString()

        return content
    }

    fun fromPaths(vararg paths: Any): SuFile? {
        val files = paths.map { SuFile(path, it) }
        for (f in files) {
            if (f.exists()) {
                return f
            }
        }

        return null
    }

    fun readBytes(): ByteArray = newInputStream().use { it.readBytes() }

    fun writeText(data: String) = newOutputStream(false).use { it.write(data.toByteArray()) }

    fun writeBytes(data: ByteArray) = newOutputStream(false).use { it.write(data) }

    override fun list(): Array<String>? = fallback(
        root = { this.list(this@SuFile.path) },
        nonRoot = { list() }
    )

    private fun getOwnPrimitiveLength(): Long {
        return fallback(
            root = {
                this.length(this@SuFile.path)
            },
            nonRoot = {
                length()
            }
        )
    }

    override fun length(): Long = this.length(recursive = false)

    suspend fun lengthAsync(): Long = withContext<Long>(Dispatchers.IO) {
        this@SuFile.length(recursive = false)
    }

    suspend fun lengthAsync(
        recursive: Boolean = false,
        skipPaths: List<String> = emptyList(),
        skipSymLinks: Boolean = true,
    ): Long = withContext<Long>(Dispatchers.IO) {
        this@SuFile.length(
            recursive = recursive,
            skipPaths = skipPaths,
            skipSymLinks = skipSymLinks,
        )
    }

    fun length(
        recursive: Boolean = false,
        skipPaths: List<String> = emptyList(),
        skipSymLinks: Boolean = true,
    ): Long = fallback(
        root = { calculateSizeInContext(recursive, skipPaths, skipSymLinks) },
        nonRoot = { calculateSizeInContext(recursive, skipPaths, skipSymLinks) }
    )

    private fun calculateSizeInContext(
        recursive: Boolean,
        skipPaths: List<String>,
        skipSymLinks: Boolean,
    ): Long {
        if (recursive) {
            if (!this.isDirectory()) {
                if (skipSymLinks && this.isSymlink()) return 0L
                return this.getOwnPrimitiveLength()
            }
            return doRecursiveScan(this, skipPaths, skipSymLinks)
        } else {
            if (skipSymLinks && this.isSymlink()) return 0L
            return this.getOwnPrimitiveLength()
        }
    }

    private fun doRecursiveScan(
        currentDirSuFile: SuFile,
        skipPaths: List<String>,
        skipSymLinks: Boolean,
    ): Long {
        val items = currentDirSuFile.list()

        if (items == null) return 0L

        var totalSize = 0L
        for (itemName in items) {
            val itemFullPath = "${currentDirSuFile.path}/$itemName"
            val itemSuFile = SuFile(itemFullPath)

            if (skipPaths.contains(itemFullPath)) {
                continue
            }

            if (skipSymLinks && itemSuFile.isSymlink()) {
                continue
            }

            totalSize += if (itemSuFile.isDirectory()) {
                itemSuFile.length(
                    recursive = true,
                    skipPaths = skipPaths,
                    skipSymLinks = skipSymLinks
                )
            } else {
                itemSuFile.getOwnPrimitiveLength()
            }
        }

        return totalSize
    }

    fun stat(): Long {
        return this.lastModified()
    }

    override fun lastModified(): Long = fallback(
        root = { this.stat(this@SuFile.path) },
        nonRoot = { lastModified() }
    )

    override fun exists(): Boolean = fallback(
        root = { this.exists(this@SuFile.path) },
        nonRoot = { exists() }
    )

    @OptIn(ExperimentalContracts::class)
    inline fun <R> exists(block: (SuFile) -> R): R? {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        return if (exists()) block(this) else null
    }

    override fun isDirectory(): Boolean = fallback(
        root = { this.isDirectory(this@SuFile.path) },
        nonRoot = { isDirectory }
    )

    override fun isFile(): Boolean = fallback(
        root = { this.isFile(this@SuFile.path) },
        nonRoot = { isFile }
    )

    fun isBlock(): Boolean = fallback(
        root = { this.isBlock(this@SuFile.path) },
        nonRoot = {
            OsConstants.S_ISBLK(getMode(this@SuFile.path))
        }
    )

    fun isCharacter(): Boolean = fallback(
        root = { this.isCharacter(this@SuFile.path) },
        nonRoot = {
            OsConstants.S_ISCHR(getMode(this@SuFile.path))
        }
    )

    fun isSymlink(): Boolean = fallback(
        root = { this.isSymlink(this@SuFile.path) },
        nonRoot = {
            OsConstants.S_ISLNK(getMode(this@SuFile.path))
        }
    )

    fun isNamedPipe(): Boolean = fallback(
        root = { this.isNamedPipe(this@SuFile.path) },
        nonRoot = {
            OsConstants.S_ISFIFO(getMode(this@SuFile.path))
        }
    )

    fun isSocket(): Boolean = fallback(
        root = { this.isSocket(this@SuFile.path) },
        nonRoot = {
            OsConstants.S_ISSOCK(getMode(this@SuFile.path))
        }
    )

    override fun mkdir(): Boolean = fallback(
        root = { this.mkdir(this@SuFile.path) },
        nonRoot = { mkdir() }
    )

    override fun mkdirs(): Boolean = fallback(
        root = { this.mkdirs(this@SuFile.path) },
        nonRoot = { mkdirs() }
    )

    override fun createNewFile(): Boolean = fallback(
        root = { this.createNewFile(this@SuFile.path) },
        nonRoot = { createNewFile() }
    )

    override fun renameTo(dest: File): Boolean = fallback(
        root = { this.renameTo(this@SuFile.path, dest.path) },
        nonRoot = { renameTo(dest) }
    )

    fun copyTo(dest: File, overwrite: Boolean = false) = fallback(
        root = { this.copyTo(this@SuFile.path, dest.path, overwrite) },
        nonRoot = { copyTo(dest, overwrite) }
    )

    override fun canExecute(): Boolean = fallback(
        root = { this.canExecute(this@SuFile.path) },
        nonRoot = { canExecute() }
    )

    override fun canRead(): Boolean = fallback(
        root = { this.canRead(this@SuFile.path) },
        nonRoot = { canRead() }
    )

    override fun canWrite(): Boolean = fallback(
        root = { this.canWrite(this@SuFile.path) },
        nonRoot = { canWrite() }
    )

    override fun delete(): Boolean = fallback(
        root = { this.delete(this@SuFile.path) },
        nonRoot = { delete() }
    )

    override fun deleteOnExit() {
        fallback(
            root = { this.deleteOnExit(this@SuFile.path) },
            nonRoot = { deleteOnExit() }
        )
    }

    override fun isHidden(): Boolean = fallback(
        root = { this.isHidden(this@SuFile.path) },
        nonRoot = { isHidden }
    )

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
        return this.setPermissions(permissions.value)
    }

    fun setPermissions(permissions: Int): Boolean = fallback(
        root = { this.setPermissions(this@SuFile.path, permissions) },
        nonRoot = { false }
    )

    fun setOwner(uid: Int, gid: Int): Boolean = fallback(
        root = { this.setOwner(this@SuFile.path, uid, gid) },
        nonRoot = { false }
    )

    override fun listFiles(): Array<SuFile>? {
        return this.list()?.map { SuFile(it, this) }?.toTypedArray()
    }

    override fun listFiles(filter: FileFilter?): Array<SuFile>? {
        val ss = list()
        val files = ArrayList<SuFile>()
        if (ss == null) return null;
        for (s in ss) {
            val f = SuFile(s, this)
            if ((filter == null) || filter.accept(f)) files.add(f)
        }
        return files.toArray(arrayOfNulls<SuFile>(files.size))
    }


    @Throws(IOException::class)
    fun newInputStream(): InputStream = fallback(
        root = {
            val pipe = ParcelFileDescriptor.createPipe()
            try {
                this.openReadStream(this@SuFile.path, pipe[1]).checkException()
            } catch (e: RemoteException) {
                pipe[0].close()
                throw IOException(e)
            } finally {
                pipe[1].close()
            }
            return@fallback ParcelFileDescriptor.AutoCloseInputStream(pipe[0])
        },
        nonRoot = { FileInputStream(this@fallback) }
    )

    @Throws(IOException::class)
    fun newOutputStream(append: Boolean): OutputStream = fallback(
        root = {
            val pipe = ParcelFileDescriptor.createPipe()
            try {
                this.openWriteStream(this@SuFile.path, pipe[0], append).checkException()
            } catch (e: RemoteException) {
                pipe[1].close()
                throw IOException(e)
            } finally {
                pipe[0].close()
            }
            return@fallback ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
        },
        nonRoot = { FileOutputStream(this@fallback, append) }
    )

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

    private fun getMode(path: String?): Int {
        return try {
            Os.lstat(path).st_mode
        } catch (e: ErrnoException) {
            0
        }
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