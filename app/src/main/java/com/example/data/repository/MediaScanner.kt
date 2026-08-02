package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class MediaScanner(private val context: Context) {

    private val supportedExtensions = setOf("mp3", "m4a", "aac", "wav", "flac", "ogg", "opus", "3gp", "m4r", "amr", "wma")

    private fun isSystemAudioPath(path: String): Boolean {
        val lower = path.lowercase(Locale.ROOT)
        return lower.contains("/system-ext/") ||
                lower.contains("/system/") ||
                lower.contains("/vendor/") ||
                lower.contains("/product/") ||
                lower.contains("/apex/") ||
                lower.contains("/ui/audio/") ||
                lower.contains("/ringtones/") ||
                lower.contains("/notifications/") ||
                lower.contains("/alarms/")
    }

    suspend fun scanDeviceAudioFiles(): List<SongEntity> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<SongEntity>()
        val seenPaths = mutableSetOf<String>()

        val collections = mutableListOf<Uri>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collections.add(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL))
            collections.add(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_INTERNAL))
        } else {
            collections.add(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            collections.add(MediaStore.Audio.Media.INTERNAL_CONTENT_URI)
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE
        )

        val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

        // 1. Scan MediaStore (External & Internal)
        for (collectionUri in collections) {
            try {
                // Remove IS_MUSIC requirement because WhatsApp, Telegram, and Downloads often don't have IS_MUSIC flag set
                val selection = "${MediaStore.Audio.Media.DURATION} >= 2000"

                context.contentResolver.query(
                    collectionUri,
                    projection,
                    selection,
                    null,
                    "${MediaStore.Audio.Media.TITLE} ASC"
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                    val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

                    while (cursor.moveToNext()) {
                        val filePath = if (dataColumn != -1) cursor.getString(dataColumn) ?: "" else ""
                        if (filePath.isNotBlank() && (seenPaths.contains(filePath) || isSystemAudioPath(filePath))) continue

                        val ext = filePath.substringAfterLast('.', "").lowercase(Locale.ROOT)
                        if (filePath.isNotBlank() && ext !in supportedExtensions) continue

                        val id = if (idColumn != -1) cursor.getLong(idColumn) else System.currentTimeMillis()
                        val title = if (titleColumn != -1) cursor.getString(titleColumn) ?: "" else ""
                        val artist = if (artistColumn != -1) cursor.getString(artistColumn) ?: "" else ""
                        val album = if (albumColumn != -1) cursor.getString(albumColumn) ?: "" else ""
                        val albumId = if (albumIdColumn != -1) cursor.getLong(albumIdColumn) else -1L
                        val durationMs = if (durationColumn != -1) cursor.getLong(durationColumn) else 0L
                        val sizeBytes = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L

                        val displayTitle = if (title.isBlank() || title.contains("<unknown>", ignoreCase = true)) {
                            File(filePath).nameWithoutExtension.ifBlank { "Track $id" }
                        } else title

                        val displayArtist = if (artist.isBlank() || artist.contains("<unknown>", ignoreCase = true)) "Unknown Artist" else artist
                        val displayAlbum = if (album.isBlank() || album.contains("<unknown>", ignoreCase = true)) "Unknown Album" else album

                        val albumArtUri = if (albumId > 0) {
                            ContentUris.withAppendedId(albumArtBaseUri, albumId).toString()
                        } else ""

                        val fileSizeMb = (sizeBytes / (1024f * 1024f)).coerceAtLeast(0.1f)

                        if (filePath.isNotBlank()) {
                            seenPaths.add(filePath)
                        }

                        songList.add(
                            SongEntity(
                                id = if (id > 0) id else filePath.hashCode().toLong(),
                                title = displayTitle,
                                artist = displayArtist,
                                album = displayAlbum,
                                genre = "Local Audio",
                                durationMs = durationMs.coerceAtLeast(2000L),
                                albumArtResId = 0,
                                albumArtUri = albumArtUri,
                                filePath = filePath,
                                fileSizeMb = fileSizeMb,
                                isFavorite = false,
                                isOnline = false,
                                lyrics = "Local offline song.",
                                playCount = 0,
                                addedTime = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Scan Filesystem directories directly (Internal storage, SD Card, Downloads, WhatsApp, Telegram, Music)
        val commonAudioFolders = listOf(
            "/storage/emulated/0/Music",
            "/storage/emulated/0/Download",
            "/storage/emulated/0/WhatsApp/Media/WhatsApp Audio",
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio",
            "/storage/emulated/0/Telegram/Telegram Audio",
            "/storage/emulated/0/Audiobooks",
            "/storage/emulated/0/Podcasts",
            "/storage/emulated/0/Documents"
        )

        for (folderPath in commonAudioFolders) {
            try {
                val folder = File(folderPath)
                if (folder.exists() && folder.isDirectory) {
                    scanDirectoryRecursive(folder, songList, seenPaths)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        songList
    }

    private fun scanDirectoryRecursive(dir: File, songList: MutableList<SongEntity>, seenPaths: MutableSet<String>) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                // Don't recurse into hidden system folders
                if (!file.name.startsWith(".")) {
                    scanDirectoryRecursive(file, songList, seenPaths)
                }
            } else {
                val path = file.absolutePath
                if (seenPaths.contains(path) || isSystemAudioPath(path)) continue
                val ext = file.extension.lowercase(Locale.ROOT)
                if (ext in supportedExtensions) {
                    seenPaths.add(path)
                    val title = file.nameWithoutExtension
                    val sizeBytes = file.length()
                    val fileSizeMb = (sizeBytes / (1024f * 1024f)).coerceAtLeast(0.1f)

                    songList.add(
                        SongEntity(
                            id = path.hashCode().toLong(),
                            title = title,
                            artist = "Local Artist",
                            album = dir.name,
                            genre = "Local Audio",
                            durationMs = 180000L, // default estimated
                            albumArtResId = 0,
                            albumArtUri = "",
                            filePath = path,
                            fileSizeMb = fileSizeMb,
                            isFavorite = false,
                            isOnline = false,
                            lyrics = "Local offline song.",
                            playCount = 0,
                            addedTime = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    suspend fun deleteFileFromDevice(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

