package dev.dergoogler.mmrl.compat.impl

import android.os.ParcelFileDescriptor
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants.O_APPEND
import android.system.OsConstants.O_CREAT
import android.system.OsConstants.O_RDONLY
import android.system.OsConstants.O_TRUNC
import android.system.OsConstants.O_WRONLY
import android.util.LruCache
import com.dergoogler.mmrl.utils.file.FileUtils
import com.dergoogler.mmrl.utils.file.OpenFile
import com.dergoogler.mmrl.utils.file.SuFile
import dev.dergoogler.mmrl.compat.content.ParcelResult
import dev.dergoogler.mmrl.compat.stub.IFileManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class FileManagerImpl : IFileManager.Stub() {

    init {
        System.loadLibrary("file-manager")
    }

    private val mCache: LruCache<String, File> = object : LruCache<String, File>(100) {
        override fun create(key: String): File {
            return File(key)
        }
    }

    private external fun nativeSetOwner(path: String, owner: Int, group: Int): Boolean
    private external fun nativeSetPermissions(path: String, mode: Int): Boolean

    override fun deleteOnExit(path: String) = with(File(path)) {
        when {
            !exists() -> false
            isFile -> delete()
            isDirectory -> deleteRecursively()
            else -> false
        }
    }

    override fun list(path: String): Array<String>? = mCache.get(path).list()
    override fun size(path: String): Long = mCache.get(path).length()
    override fun sizeRecursive(path: String): Long {
        val items = list(path) ?: return 0
        return items.sumOf { item ->
            val fullPath = "$path/$item"
            val path = Path.of(fullPath)
            val isSymlink = Files.isSymbolicLink(path)
            
            if (isSymlink) return@sumOf 0
            
            if (isDirectory(fullPath)) {
                sizeRecursive(fullPath)
            } else {
                size(fullPath)
            }
        }
    }

    override fun stat(path: String): Long = mCache.get(path).lastModified()
    override fun delete(path: String): Boolean {
        val f = mCache.get(path)

        return when {
            !f.exists() -> false
            f.isFile -> f.delete()
            f.isDirectory -> f.deleteRecursively()
            else -> false
        }
    }

    override fun exists(path: String): Boolean = mCache.get(path).exists()
    override fun isDirectory(path: String): Boolean = mCache.get(path).isDirectory
    override fun isFile(path: String): Boolean = mCache.get(path).isFile
    override fun mkdir(path: String): Boolean = mCache.get(path).mkdir()
    override fun mkdirs(path: String): Boolean = mCache.get(path).mkdirs()
    override fun createNewFile(path: String): Boolean = mCache.get(path).createNewFile()
    override fun renameTo(target: String, dest: String): Boolean =
        mCache.get(target).renameTo(mCache.get(dest))

    override fun copyTo(
        path: String,
        target: String,
        overwrite: Boolean,
    ) {
        mCache.get(path).copyTo(mCache.get(target), overwrite)
    }

    override fun canExecute(path: String): Boolean = mCache.get(path).canExecute()
    override fun canWrite(path: String): Boolean = mCache.get(path).canWrite()
    override fun canRead(path: String): Boolean = mCache.get(path).canRead()
    override fun isHidden(path: String): Boolean = mCache.get(path).isHidden
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

    private val streamPool: ExecutorService = Executors.newCachedThreadPool()

    override fun openReadStream(path: String, fd: ParcelFileDescriptor): ParcelResult {
        val f = OpenFile()
        try {
            f.fd = Os.open(path, O_RDONLY, 0)
            streamPool.execute {
                runCatching {
                    f.use { of ->
                        of.write = FileUtils.createFileDescriptor(fd.detachFd())
                        while (of.pread(SuFile.PIPE_CAPACITY, -1) > 0);
                    }
                }
            }
            return ParcelResult()
        } catch (e: ErrnoException) {
            f.close()
            return ParcelResult(e)
        }
    }

    override fun openWriteStream(
        path: String,
        fd: ParcelFileDescriptor,
        append: Boolean,
    ): ParcelResult {
        val f = OpenFile()
        try {
            val mode = O_CREAT or O_WRONLY or (if (append) O_APPEND else O_TRUNC)
            f.fd = Os.open(path, mode, 438)
            streamPool.execute {
                runCatching {
                    f.use { of ->
                        of.read = FileUtils.createFileDescriptor(fd.detachFd())
                        while (of.pwrite(SuFile.PIPE_CAPACITY.toLong(), -1, false) > 0);
                    }
                }
            }
            return ParcelResult()
        } catch (e: ErrnoException) {
            f.close()
            return ParcelResult(e)
        }
    }
}
