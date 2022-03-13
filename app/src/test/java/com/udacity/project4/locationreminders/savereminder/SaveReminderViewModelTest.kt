package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.ExpectFailure.assertThat
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import java.util.regex.Matcher

@Config(sdk = [Build.VERSION_CODES.O_MR1])

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private var lat = 30.081313382204318
    private var long = 31.364997535209202

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun after() {
        stopKoin()
    }

    private fun getFakeReminderData(): ReminderDataItem {
        return ReminderDataItem(
            title = "title",
            description = "description",
            location = "location",
            latitude = lat,
            longitude = long
        )
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        val reminder = getFakeReminderData()
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            Matchers.`is`(true)
        )
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            Matchers.`is`(false)
        )
    }

    @Test
    fun check_error_noTitle() = mainCoroutineRule.runBlockingTest {

        val reminderNoTitle = ReminderDataItem(null,
            "Description",
            "Location",
            lat, long)

        Truth.assertThat(saveReminderViewModel.validateEnteredData(reminderNoTitle)).isFalse()

        Assert.assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(
            R.string.err_enter_title))
    }
}



