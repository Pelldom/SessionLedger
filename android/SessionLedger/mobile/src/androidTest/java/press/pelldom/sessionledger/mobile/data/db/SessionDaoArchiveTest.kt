package press.pelldom.sessionledger.mobile.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

@RunWith(AndroidJUnit4::class)
class SessionDaoArchiveTest {
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun activeAndArchivedQueriesFilterCorrectly() = runBlocking {
        val dao = db.sessionDao()

        val active = SessionEntity(
            id = "s_active",
            startTimeMs = 1000L,
            endTimeMs = 2000L,
            state = SessionState.ENDED,
            pausedTotalMs = 0L,
            lastStateChangeTimeMs = 2000L,
            isArchived = false,
            archivedAtMillis = null,
            categoryId = DefaultCategory.UNCATEGORIZED_ID,
            notes = null,
            createdOnDevice = "phone",
            updatedAtMs = 2000L
        )

        val archived = SessionEntity(
            id = "s_archived",
            startTimeMs = 3000L,
            endTimeMs = 4000L,
            state = SessionState.ENDED,
            pausedTotalMs = 0L,
            lastStateChangeTimeMs = 4000L,
            isArchived = true,
            archivedAtMillis = 5000L,
            categoryId = DefaultCategory.UNCATEGORIZED_ID,
            notes = null,
            createdOnDevice = "phone",
            updatedAtMs = 4000L
        )

        dao.insert(active)
        dao.insert(archived)

        val activeList = dao.observeActiveEndedSessionsNewestFirst().first()
        val archivedList = dao.observeArchivedEndedSessionsNewestFirst().first()

        assertEquals(listOf("s_active"), activeList.map { it.id })
        assertEquals(listOf("s_archived"), archivedList.map { it.id })

        val inRange = dao.getEndedSessionsInRange(startMs = 0L, endMs = 10_000L)
        assertEquals(listOf("s_active"), inRange.map { it.id })
    }
}

