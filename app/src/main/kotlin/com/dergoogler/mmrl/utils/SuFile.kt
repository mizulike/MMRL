package com.dergoogler.mmrl.utils

import com.dergoogler.mmrl.Compat
import dev.dergoogler.mmrl.compat.core.BrickException
import java.io.File
import java.io.FileFilter

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


    companion object {
        enum class Permissions(val value: Int) {
            OWNER_READ(0b100000000),  // 0o400 (256) - r-- for owner
            OWNER_WRITE(0b010000000), // 0o200 (128) - -w- for owner
            OWNER_EXECUTE(0b001000000), // 0o100 (64) - --x for owner

            GROUP_READ(0b000100000), // 0o040 (32) - r-- for group
            GROUP_WRITE(0b000010000), // 0o020 (16) - -w- for group
            GROUP_EXECUTE(0b000001000), // 0o010 (8) - --x for group

            OTHERS_READ(0b000000100), // 0o004 (4) - r-- for others
            OTHERS_WRITE(0b000000010), // 0o002 (2) - -w- for others
            OTHERS_EXECUTE(0b000000001), // 0o001 (1) - --x for others

            PERMISSION_777(0b111111111), // 0o777 (511) - rwxrwxrwx
            PERMISSION_755(0b111101101), // 0o755 (493) - rwxr-xr-x
            PERMISSION_700(0b111000000), // 0o700 (448) - rwx------
            PERMISSION_644(0b110100100), // 0o644 (420) - rw-r--r--
            PERMISSION_600(0b110000000), // 0o600 (384) - rw-------
            PERMISSION_444(0b100100100); // 0o444 (292) - r--r--r--

            companion object {
                fun combine(vararg permissions: Permissions): Int {
                    return permissions.fold(0) { acc, perm -> acc or perm.value }
                }
            }
        }
    }

    fun readText(): String {
        return fs.readText(this.path)
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

    fun list(fullPath: Boolean): Array<String> {
        return fs.list(this.path, fullPath)
    }

    override fun list(): Array<String> {
        return fs.list(this.path, false)
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
            Permissions.combine(
                Permissions.OWNER_READ,
                Permissions.GROUP_READ,
                Permissions.OTHERS_READ,
            )
        )
    }

    override fun setExecutable(executable: Boolean): Boolean {
        return setPermissions(Permissions.PERMISSION_755)
    }

    fun setPermissions(permissions: Permissions): Boolean {
        return fs.setPermissions(this.path, permissions.value)
    }

    fun setPermissions(permissions: Int): Boolean {
        return fs.setPermissions(this.path, permissions)
    }

    fun setOwner(uid: Int, gid: Int): Boolean {
        return fs.setOwner(this.path, uid, gid)
    }


    override fun listFiles(): Array<SuFile?> {
        val ss = this.list(true)
        val n = ss.size
        val fs = arrayOfNulls<SuFile>(n)
        for (i in 0..<n) {
            fs[i] = SuFile(ss[i], this)
        }
        return fs
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