package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest;
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersDaoTest {

    private lateinit var localDataSource: FakeDataSource
    private lateinit var database: RemindersDatabase
    private var lat = 30.081313382204318
    private var long = 31.364997535209202

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private fun getFakeReminderData(): ReminderDTO {
        return ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = lat,
            longitude = long
        )
    }

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminders() = runBlockingTest {
        // GIVEN - A new reminder saved in the database.
        val reminder = getFakeReminderData()
        database.reminderDao().saveReminder(reminder)
        // WHEN  - Task retrieved.
        val reminders = database.reminderDao().getReminders()
        // THEN - Same reminder is returned.
        Assert.assertThat(reminders[0].id, `is`(reminder.id))
        Assert.assertThat(reminders[0].title, `is`(reminder.title))
        Assert.assertThat(reminders[0].description, `is`(reminder.description))
        Assert.assertThat(reminders[0].location, `is`(reminder.location))
        Assert.assertThat(reminders[0].latitude, `is`(reminder.latitude))
        Assert.assertThat(reminders[0].longitude, `is`(reminder.longitude))

    }

    @Test
    fun insert_ReminderGetById() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = getFakeReminderData()
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        Assert.assertThat(loaded.id, `is`(reminder.id))
        Assert.assertThat(loaded.title, `is`(reminder.title))
        Assert.assertThat(loaded.description, `is`(reminder.description))
        Assert.assertThat(loaded.location, `is`(reminder.location))
        Assert.assertThat(loaded.latitude, `is`(reminder.latitude))
        Assert.assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        val reminder1 = getFakeReminderData()
        val reminder2 = getFakeReminderData()
        val reminder3 = getFakeReminderData()
        val remindersList : MutableList<ReminderDTO>? = mutableListOf(reminder1,reminder2,reminder3)

        remindersList?.forEach {
            database.reminderDao().saveReminder(it)
        }

        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.isEmpty(), `is`(true))
    }

    @Test
    fun getReminderByID_IDNotFound() = runBlockingTest {
        val reminderId = UUID.randomUUID().toString()
        val loaded = database.reminderDao().getReminderById(reminderId)
        Assert.assertNull(loaded)
    }

}