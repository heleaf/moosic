package com.dev.moosic
import com.dev.moosic.controllers.TestSongControllerImpl
import com.dev.moosic.controllers.TestSongControllerInterface
import com.dev.moosic.models.UserRepositorySong
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class SongControllerUnitTest {
    class MockUserRepository() : UserRepositoryInterface {
        private var userRepositorySongs = ArrayList<UserRepositorySong>()
        override fun getUserPlaylistSongs(): ArrayList<UserRepositorySong> {
            return userRepositorySongs
        }
        override fun addSongToUserPlaylist(song: UserRepositorySong) {
            userRepositorySongs.add(song)
        }
        override fun removeSongFromUserPlaylist(songId: String) {
            for (song in userRepositorySongs) {
                if (song.id == songId) userRepositorySongs.remove(song); return;
            }
        }
        override fun logSongInModel(song: UserRepositorySong, weight: Int) { }
        override fun isInUserPlaylist(songId: String): Boolean {
            val songIds = userRepositorySongs.map{song -> song.id}
            return songId in songIds
        }

        override fun setCurrentSong(song: UserRepositorySong) {}
        override fun playSong(songId: String) {}
        override fun getCurrentSong(): UserRepositorySong? { return null }
        override fun getCurrentSongIsPlaying(): Boolean? { return null }
        override fun pauseSong() {}
        override fun resumeSong() {}
    }

    @Test
    fun addToPlaylist_creates_no_duplicates(){
        val mockUserRepository = spyk<MockUserRepository>()
        val song = UserRepositorySong("1", "{}")
        val controller : TestSongControllerInterface = TestSongControllerImpl(mockUserRepository)
        controller.addToPlaylist(song)
        verify(exactly = 1) { mockUserRepository.addSongToUserPlaylist(song) }
        verify(exactly = 1) { mockUserRepository.isInUserPlaylist(song.id) }

        controller.addToPlaylist(song)
        verify(exactly = 1) { mockUserRepository.addSongToUserPlaylist(song) }
        verify(exactly = 2) { mockUserRepository.isInUserPlaylist(song.id) }
    }

    @Test
    fun removeFromPlaylist_checks_isInPlaylist(){
        val mockUserRepository = spyk<MockUserRepository>()
        val song = UserRepositorySong("1", "{}")
        val controller = TestSongControllerImpl(mockUserRepository)
        controller.addToPlaylist(song)
        controller.removeFromPlaylist(song)
        verify(exactly = 2) { mockUserRepository.isInUserPlaylist(song.id) }
        verify(exactly = 1) { mockUserRepository.addSongToUserPlaylist(song) }
        verify(exactly = 1) { mockUserRepository.removeSongFromUserPlaylist(song.id) }

        controller.removeFromPlaylist(song)
        verify(exactly = 3) { mockUserRepository.isInUserPlaylist(song.id) }
        verify(exactly = 1) { mockUserRepository.removeSongFromUserPlaylist(song.id) }
    }

}