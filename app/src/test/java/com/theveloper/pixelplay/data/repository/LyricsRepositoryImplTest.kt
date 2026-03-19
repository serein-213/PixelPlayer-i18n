package com.theveloper.pixelplay.data.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.theveloper.pixelplay.data.database.LyricsDao
import com.theveloper.pixelplay.data.model.LyricsSourcePreference
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.network.lyrics.LrcLibApiService
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test

class LyricsRepositoryImplTest {

    @Test
    fun getLyrics_returnsSongLyricsBeforeNeedingStorageRead() = runTest {
        val repository = LyricsRepositoryImpl(
            context = mockk<Context>(relaxed = true),
            lrcLibApiService = mockk<LrcLibApiService>(relaxed = true),
            lyricsDao = mockk<LyricsDao>(relaxed = true),
            okHttpClient = mockk<OkHttpClient>(relaxed = true)
        )
        val song = Song(
            id = "12",
            title = "Track",
            artist = "Artist",
            artistId = 5L,
            album = "Album",
            albumId = 8L,
            path = "",
            contentUriString = "",
            albumArtUriString = null,
            duration = 180_000L,
            lyrics = "[00:01.00]Hello again",
            mimeType = "audio/mpeg",
            bitrate = 320_000,
            sampleRate = 44_100
        )

        val lyrics = repository.getLyrics(song, LyricsSourcePreference.EMBEDDED_FIRST)

        assertThat(lyrics).isNotNull()
        assertThat(lyrics!!.areFromRemote).isFalse()
        assertThat(lyrics.synced).isNotEmpty()
        assertThat(lyrics.synced!!.first().line).isEqualTo("Hello again")
    }
}
