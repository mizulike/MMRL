package com.dergoogler.mmrl.utils.file

import android.os.ParcelFileDescriptor
import android.os.RemoteException
import com.dergoogler.mmrl.Compat
import dev.dergoogler.mmrl.compat.core.BrickException
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

private val fs = Compat.fileManager

class SuFile(path: String) : File(path) {

    constructor(path: String, parent: File) : this(File(parent, path).path)
    constructor(vararg paths: String) : this(fs.resolve(paths))

    companion object {
        const val PIPE_CAPACITY = 16 * 4096

        fun String.toSuFile(): SuFile {
            return SuFile(this)
        }
    }

    constructor(vararg paths: Any) : this(
        fs.resolve(paths.map {
            when (it) {
                is File -> it.path
                is String -> it
                else -> throw BrickException("Unsupported type: ${it::class}")
            }
        }.toTypedArray())
    )

    fun readText(): String {
        val bytes = newInputStream().use { it.readBytes() }
        val data = ByteBuffer.wrap(bytes)
        val content = StandardCharsets.UTF_8.decode(data).toString()

        return content
    }

    fun parcelStream(): FileInputStream {
        val fd = fs.parcelFile(this.path)
        return FileInputStream(fd.fileDescriptor)
    }

    fun readBytes(): ByteArray = newInputStream().use { it.readBytes() }

    fun writeText(data: String) = newOutputStream(false).use { it.write(data.toByteArray()) }

    fun writeBytes(data: ByteArray) = newOutputStream(false).use { it.write(data) }

    override fun list(): Array<String> {
        return fs.list(this.path)
    }

    override fun length(): Long {
        return this.size(false)
    }

    fun size(recursively: Boolean = false): Long {
        if (recursively) {
            return fs.sizeRecursive(this.path)
        }

        return fs.size(this.path)
    }

    fun stat(): Long {
        return this.lastModified()
    }

    override fun lastModified(): Long {
        return fs.stat(this.path)
    }

    override fun exists(): Boolean {
        return fs.exists(this.path)
    }

    override fun isDirectory(): Boolean {
        return fs.isDirectory(this.path)
    }

    override fun isFile(): Boolean {
        return fs.isFile(this.path)
    }

    override fun mkdir(): Boolean {
        return fs.mkdir(this.path)
    }

    override fun mkdirs(): Boolean {
        return fs.mkdirs(this.path)
    }

    override fun createNewFile(): Boolean {
        return fs.createNewFile(this.path)
    }

    override fun renameTo(dest: File): Boolean {
        return fs.renameTo(this.path, dest.path)
    }

    fun copyTo(dest: File, overwrite: Boolean = false) {
        return fs.copyTo(this.path, dest.path, overwrite)
    }

    override fun canExecute(): Boolean {
        return fs.canExecute(this.path)
    }

    override fun canRead(): Boolean {
        return fs.canRead(this.path)
    }

    override fun canWrite(): Boolean {
        return fs.canWrite(this.path)
    }

    override fun delete(): Boolean {
        return fs.delete(this.path)
    }

    override fun deleteOnExit() {
        fs.deleteOnExit(this.path)
    }

    override fun isHidden(): Boolean {
        return fs.isHidden(this.path)
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
        return fs.setPermissions(this.path, permissions.value)
    }

    fun setPermissions(permissions: Int): Boolean {
        return fs.setPermissions(this.path, permissions)
    }

    fun setOwner(uid: Int, gid: Int): Boolean {
        return fs.setOwner(this.path, uid, gid)
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
            fs.openReadStream(this.path, pipe[1]).checkException()
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
            fs.openWriteStream(this.path, pipe[0], append).checkException()
        } catch (e: RemoteException) {
            pipe[1].close()
            throw IOException(e)
        } finally {
            pipe[0].close()
        }
        return ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
    }
}