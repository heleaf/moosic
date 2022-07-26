package com.dev.moosic
import com.dev.moosic.controllers.UserRepoPlaylistController
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import com.dev.moosic.models.UserRepositorySong
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import com.parse.ParseUser

class SongControllerUnitTest {
    class MockUserRepository() : UserRepositoryInterface {
        private var userRepositorySongs = ArrayList<UserRepositorySong>()

        override fun getUser(): ParseUser? {
            return null
        }

        override fun getUserPlaylistSongs(): ArrayList<UserRepositorySong> {
            return userRepositorySongs
        }

        override fun addSongToUserPlaylist(song: UserRepositorySong, save: Boolean) {
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

        override fun getSongWithId(id: String): UserRepositorySong? {
            for (song in userRepositorySongs){
                if (song.id == id) return song
            }
            return null
        }

        override fun toast(message: String) {}
    }

    @Test
    fun addToPlaylist_creates_no_duplicates(){
        val mockUserRepository = spyk<MockUserRepository>()
        val song = UserRepositorySong("1", "{}")
        val controller : UserRepoPlaylistControllerInterface = UserRepoPlaylistController(mockUserRepository)
        controller.addToPlaylist(song, false)
        verify(exactly = 1) { mockUserRepository.addSongToUserPlaylist(song, false) }
        verify(exactly = 1) { mockUserRepository.isInUserPlaylist(song.id) }

        controller.addToPlaylist(song, false)
        verify(exactly = 1) { mockUserRepository.addSongToUserPlaylist(song, false) }
        verify(exactly = 2) { mockUserRepository.isInUserPlaylist(song.id) }
    }

    @Test
    fun removeFromPlaylist_checks_isInPlaylist(){
        val mockUserRepository = spyk<MockUserRepository>()
        val song = UserRepositorySong("1", "{}")
        val controller = UserRepoPlaylistController(mockUserRepository)
        controller.addToPlaylist(song, false)
        controller.removeFromPlaylist(song)
        verify(exactly = 2) { mockUserRepository.isInUserPlaylist(song.id) }
        verify(exactly = 1) { mockUserRepository.addSongToUserPlaylist(song, false) }
        verify(exactly = 1) { mockUserRepository.removeSongFromUserPlaylist(song.id) }

        controller.removeFromPlaylist(song)
        verify(exactly = 3) { mockUserRepository.isInUserPlaylist(song.id) }
        verify(exactly = 1) { mockUserRepository.removeSongFromUserPlaylist(song.id) }
    }

}