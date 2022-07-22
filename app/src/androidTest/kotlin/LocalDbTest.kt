import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dev.moosic.localdb.LocalDatabase
import com.dev.moosic.localdb.daos.UserDao
import com.dev.moosic.localdb.entities.SavedUser
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TAG = "LocalDbUnitTest"

@RunWith(AndroidJUnit4::class)
class LocalDbUnitTest {
    private lateinit var userDao: UserDao
    private lateinit var db: LocalDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, LocalDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        userDao = db.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testUserDao() {
        val username = "bob"
        val parseUserId = "1"
        val savedSongs = "[ann]"
        val user = SavedUser(username, parseUserId, savedSongs)

        val songs = userDao.getUserSavedSongs(username)
        assertEquals(songs.size, 0)
        val foundUser = userDao.getUser(username)
        assertEquals(foundUser, null)

        userDao.insertUserInfo(user)
        val foundUser1 = userDao.getUser(username)
        assertEquals(foundUser1.savedSongs, savedSongs)
        assertEquals(foundUser1.parseUsername, username)
        assertEquals(foundUser1.parseUserId, parseUserId)

        val foundSongs1 = userDao.getUserSavedSongs(username)
        assertEquals(foundSongs1.size, 1)
        assertEquals(foundSongs1.get(0), savedSongs)

        val identicalUser = SavedUser(username, parseUserId, savedSongs)
        try { userDao.insertUserInfo(identicalUser) }
        catch (exception: SQLiteConstraintException) { }

        val username2 = "alice"
        val parseUserId2 = "2"
        val savedSongs2 = "[wonderland, bunny]"
        val newUser = SavedUser(username2, parseUserId2, savedSongs2)
        userDao.updateUserInfo(newUser) // note: does not raise error
        val foundUser2 = userDao.getUser(username2)
        assertEquals(foundUser2, null)

        val updatedSavedSongs = "[ann, green]"
        val updatedUser = SavedUser(username, parseUserId, updatedSavedSongs)
        userDao.updateUserInfo(updatedUser)
        val foundUser3 = userDao.getUser(username)
        assertNotNull(foundUser3)
        assertEquals(foundUser3.parseUsername, username)
        assertEquals(foundUser3.parseUserId, parseUserId)
        assertEquals(foundUser3.savedSongs, updatedSavedSongs)

        userDao.deleteUserInfo(newUser) // does not raise error
        assertNull(userDao.getUser(newUser.parseUsername))

        userDao.deleteUserInfo(user)
        assertNull(userDao.getUser(user.parseUsername))

        userDao.insertUserInfo(newUser)
        assertNotNull(userDao.getUser(newUser.parseUsername))
        userDao.deleteUserInfo(newUser)
        assertNull(userDao.getUser(newUser.parseUsername))
    }
}