package com.dergoogler.mmrl.utils.file

import com.dergoogler.mmrl.Compat
import dev.dergoogler.mmrl.compat.core.BrickException
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream

private val fs = Compat.fileManager

class SuFile(path: String) : File(path) {

    constructor(path: String, parent: File) : this(File(parent, path).path)
    constructor(vararg paths: String) : this(fs.resolve(paths))

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
        return fs.readText(this.path)
    }

    fun parcelStream(): FileInputStream {
        val fd = fs.parcelFile(this.path)
        return FileInputStream(fd.fileDescriptor)
    }

    fun readBytes(): ByteArray {
        return fs.readBytes(this.path)
    }

    fun readAsBase64(): String {
        return fs.readAsBase64(this.path)
    }

    fun writeText(data: String): Boolean {
        return fs.writeText(this.path, data)
    }

    fun writeBytes(data: ByteArray): Boolean {
        return fs.writeBytes(this.path, data)
    }

    override fun list(): Array<String> {
        return fs.list(this.path)
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

    fun copyTo(dest: File, overwrite: Boolean = false): Boolean {
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
}