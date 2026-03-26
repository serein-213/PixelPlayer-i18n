package com.theveloper.pixelplay.data.media

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.kyant.taglib.TagLib
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import timber.log.Timber
import java.io.File

data class AudioMetadata(
    val title: String?,
    val artist: String?,
    val albumArtist: String?,
    val album: String?,
    val genre: String?,
    val lyrics: String?,
    val durationMs: Long?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val bitrate: Int?,
    val sampleRate: Int?,
    val artwork: AudioMetadataArtwork?
)

data class AudioMetadataArtwork(
    val bytes: ByteArray,
    val mimeType: String?
)

object AudioMetadataReader {

    private const val TAG = "AudioMetadataReader"

    fun read(context: Context, uri: Uri): AudioMetadata? {
        val tempFile = createTempAudioFileFromUri(context, uri) ?: run {
            Timber.tag(TAG).w("Unable to create temp file for uri: $uri")
            return null
        }

        return try {
            read(tempFile)
        } finally {
            try {
                tempFile.delete()
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Failed to delete temp file")
            }
        }
    }

    fun read(file: File, readArtwork: Boolean = true): AudioMetadata? {
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                // Get audio properties for duration
                val audioProperties = TagLib.getAudioProperties(fd.dup().detachFd())
                val durationMs = audioProperties?.length?.takeIf { it > 0 }?.let { it * 1000L }
                val bitrate = audioProperties?.bitrate?.takeIf { it > 0 }
                val sampleRate = audioProperties?.sampleRate?.takeIf { it > 0 }

                // Get metadata
                val metadata = TagLib.getMetadata(fd.dup().detachFd(), readPictures = false)
                val propertyMap = metadata?.propertyMap ?: emptyMap()

                // Log ALL keys TagLib returned so we can diagnose mapping issues
                Log.w(TAG, "TagLib propertyMap keys for ${file.name}: ${propertyMap.keys}")

                val title = propertyMap["TITLE"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                val artist = propertyMap["ARTIST"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                val albumArtist = propertyMap["ALBUMARTIST"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: propertyMap["ALBUM ARTIST"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: propertyMap["BAND"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                val album = propertyMap["ALBUM"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                val genre = propertyMap["GENRE"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                val lyrics = propertyMap["LYRICS"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: propertyMap["UNSYNCEDLYRICS"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                val trackString = propertyMap["TRACKNUMBER"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: propertyMap["TRACK"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                val trackNumber = trackString?.substringBefore('/')?.toIntOrNull()
                val discString = propertyMap["DISCNUMBER"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: propertyMap["DISC"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                val discNumber = discString?.substringBefore('/')?.toIntOrNull()
                val year = propertyMap["DATE"]?.firstOrNull()?.takeIf { it.isNotBlank() }?.take(4)?.toIntOrNull()
                    ?: propertyMap["YEAR"]?.firstOrNull()?.takeIf { it.isNotBlank() }?.toIntOrNull()

                Log.w(TAG, "TagLib result for ${file.name}: title=$title, artist=$artist, album=$album, genre=$genre")

                // Get artwork only when requested to avoid allocating large ByteArrays unnecessarily
                val artwork = if (readArtwork) {
                    val pictures = TagLib.getPictures(fd.detachFd())
                    pictures.firstOrNull()?.let { picture ->
                        picture.data.takeIf { it.isNotEmpty() && isValidImageData(it) }?.let { data ->
                            AudioMetadataArtwork(
                                bytes = data,
                                mimeType = picture.mimeType.takeIf { it.isNotBlank() } ?: guessImageMimeType(data)
                            )
                        }
                    }
                } else {
                    null
                }

                // Fallback: TagLib sometimes parses core tags but misses APIC/other ID3 frames
                // on some MP3s. If essential fields or requested artwork are missing, try
                // JAudioTagger before giving up so we preserve full metadata when possible.
                val fallback = if (title == null || artist == null || (readArtwork && artwork == null)) {
                    Log.w(TAG, "TagLib incomplete for ${file.name}, trying JAudioTagger fallback...")
                    readWithJAudioTagger(file)
                } else null

                AudioMetadata(
                    title = title ?: fallback?.title,
                    artist = artist ?: fallback?.artist,
                    albumArtist = albumArtist ?: fallback?.albumArtist,
                    album = album ?: fallback?.album,
                    genre = genre ?: fallback?.genre,
                    lyrics = lyrics ?: fallback?.lyrics,
                    durationMs = durationMs ?: fallback?.durationMs,
                    trackNumber = trackNumber ?: fallback?.trackNumber,
                    discNumber = discNumber ?: fallback?.discNumber,
                    year = year ?: fallback?.year,
                    bitrate = bitrate ?: fallback?.bitrate,
                    sampleRate = sampleRate ?: fallback?.sampleRate,
                    artwork = artwork ?: fallback?.artwork
                )
            }
        } catch (error: Exception) {
            Timber.tag(TAG).e(error, "Unable to read metadata from file: ${file.absolutePath}")
            null
        }
    }

    /**
     * Fallback reader using JAudioTagger for files where TagLib can't map ID3 frames.
     * Called when TagLib leaves key metadata or requested artwork unresolved.
     */
    private fun readWithJAudioTagger(file: File): AudioMetadata? {
        return try {
            // Suppress JAudioTagger's verbose logging
            java.util.logging.Logger.getLogger("org.jaudiotagger").level = java.util.logging.Level.OFF

            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            val header = audioFile.audioHeader

            Log.w(TAG, "JAudioTagger: tag class=${tag?.javaClass?.simpleName}, " +
                    "header=${header?.format}, sampleRate=${header?.sampleRateAsNumber}")

            val title = tag?.getFirst(FieldKey.TITLE)?.takeIf { it.isNotBlank() }
            val artist = tag?.getFirst(FieldKey.ARTIST)?.takeIf { it.isNotBlank() }
            val albumArtist = tag?.getFirst(FieldKey.ALBUM_ARTIST)?.takeIf { it.isNotBlank() }
            val album = tag?.getFirst(FieldKey.ALBUM)?.takeIf { it.isNotBlank() }
            val genre = tag?.getFirst(FieldKey.GENRE)?.takeIf { it.isNotBlank() }
            val lyrics = tag?.getFirst(FieldKey.LYRICS)?.takeIf { it.isNotBlank() }
            val trackNumber = tag?.getFirst(FieldKey.TRACK)?.takeIf { it.isNotBlank() }
                ?.substringBefore('/')?.toIntOrNull()
            val discNumber = tag?.getFirst(FieldKey.DISC_NO)?.takeIf { it.isNotBlank() }
                ?.substringBefore('/')?.toIntOrNull()
            val year = tag?.getFirst(FieldKey.YEAR)?.takeIf { it.isNotBlank() }
                ?.take(4)?.toIntOrNull()

            val durationMs = header?.trackLength?.takeIf { it > 0 }?.let { it * 1000L }
            val bitrate = header?.bitRateAsNumber?.takeIf { it > 0 }?.toInt()
            val sampleRate = header?.sampleRateAsNumber?.takeIf { it > 0 }

            // Try to get artwork from JAudioTagger
            val artwork = tag?.firstArtwork?.let { art ->
                art.binaryData?.takeIf { it.isNotEmpty() && isValidImageData(it) }?.let { data ->
                    AudioMetadataArtwork(
                        bytes = data,
                        mimeType = art.mimeType?.takeIf { it.isNotBlank() } ?: guessImageMimeType(data)
                    )
                }
            }

            Log.w(TAG, "JAudioTagger result for ${file.name}: title=$title, artist=$artist, " +
                    "album=$album, genre=$genre, artwork=${artwork != null}")

            AudioMetadata(
                title = title,
                artist = artist,
                albumArtist = albumArtist,
                album = album,
                genre = genre,
                lyrics = lyrics,
                durationMs = durationMs,
                trackNumber = trackNumber,
                discNumber = discNumber,
                year = year,
                bitrate = bitrate,
                sampleRate = sampleRate,
                artwork = artwork
            )
        } catch (e: Exception) {
            Log.e(TAG, "JAudioTagger fallback FAILED for: ${file.name}", e)
            null
        }
    }
}
