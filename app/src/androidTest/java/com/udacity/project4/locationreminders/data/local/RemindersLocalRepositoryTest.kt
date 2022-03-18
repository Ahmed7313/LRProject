package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase
    //    private val testDispatcher = TestCoroutineDispatcher()
//    private val testScope = TestCoroutineScope(testDispatcher)
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }


    @After
    fun cleanUp(){
        database.close()
    }

    private fun getReminder(): ReminderDTO {
        return ReminderDTO(
            title = "Title",
            description = "description",
            location = "Cairo",
            latitude = 1.1,
            longitude = 2.2)
    }

    @Test
    fun saveReminder_getReminder() = mainCoroutineRule.runBlockingTest {
        val reminder = getReminder()
        remindersLocalRepository.saveReminder(reminder)
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
    }

    @Test
    fun saveReminder_getReminderByID() = mainCoroutineRule.runBlockingTest {
        val reminder = getReminder()
        remindersLocalRepository.saveReminder(reminder)
        val loadedReminder = (remindersLocalRepository.getReminder(reminder.id) as? Result.Success)?.data

        Assert.assertThat<ReminderDTO>(loadedReminder as ReminderDTO, CoreMatchers.notNullValue())
        Assert.assertThat(loadedReminder, `is`(reminder))
    }

    @Test
    fun deleteAllReminders()= mainCoroutineRule.runBlockingTest {
        val reminder1 = getReminder()
        val reminder2 = getReminder()
        val reminder3 = getReminder()
        val remindersList : MutableList<ReminderDTO>? = mutableListOf(reminder1,reminder2,reminder3)
        remindersList?.forEach {
            remindersLocalRepository.saveReminder(it)
        }
        remindersLocalRepository.deleteAllReminders()
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data).isEmpty()
    }

    @Test
    fun noReminderFound_ErrorMessage() = runBlockingTest{
       val reminderMessage =(remindersLocalRepository.getReminder(getReminder().id)as Result.Error).message
        Assert.assertThat<String>(reminderMessage,CoreMatchers.notNullValue())
        assertThat(reminderMessage).isEqualTo("Reminder not found!")
    }

    @Test
    fun loadReminderWhenTasksAreUnavailable_callErrorToDisplay() = runBlockingTest {
        val reminder = remindersLocalRepository.getReminder(getReminder().id)
        assertThat(reminder).isInstanceOf(Result.Error::class.java)

        reminder as Result.Error

        assertThat(reminder.message).isEqualTo("Reminder not found!")
        assertThat(reminder.statusCode).isNull()
    }

}