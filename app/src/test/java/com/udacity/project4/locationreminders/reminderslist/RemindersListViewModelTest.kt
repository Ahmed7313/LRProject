package com.udacity.project4.locationreminders.reminderslist

import android.annotation.SuppressLint
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.*
import org.robolectric.annotation.Config
import org.hamcrest.core.Is.`is`

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private var lat = 30.081313382204318
    private var long = 31.364997535209202

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        reminderListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun after() {
        stopKoin()
    }


    private fun getFakeReminderData(): ReminderDTO {
        return ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = lat,
            longitude = long
        )
    }

    val reminder1 = getFakeReminderData()
    val reminder2 = getFakeReminderData()
    val reminder3 = getFakeReminderData()
    val remindersList : MutableList<ReminderDTO>? = mutableListOf(reminder1,reminder2,reminder3)

    @SuppressLint("CheckResult")
    @Test
    fun get_Reminders() = mainCoroutineRule.runBlockingTest {
        fakeDataSource = FakeDataSource(remindersList)
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.remindersList.getOrAwaitValue().isNotEmpty())
    }

    @Test
    fun check_loading()= mainCoroutineRule.runBlockingTest {
        fakeDataSource = FakeDataSource(remindersList)
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        Assert.assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        mainCoroutineRule.resumeDispatcher()
        Assert.assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun return_error()= mainCoroutineRule.runBlockingTest {
        fakeDataSource = FakeDataSource(null)
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        reminderListViewModel.loadReminders()
        Assert.assertThat(
            reminderListViewModel.showSnackBar.getOrAwaitValue(),
            CoreMatchers.`is`("No reminders found")
        )
    }

    @Test
    fun shouldReturnError() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.setReturnError(true)
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showSnackBar.getOrAwaitValue()).isNotEmpty()
    }

    @Test
    fun deletAllReminders() = mainCoroutineRule.runBlockingTest{
        fakeDataSource = FakeDataSource(remindersList)
        fakeDataSource.deleteAllReminders()
        reminderListViewModel.loadReminders()
        Assert.assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }
}