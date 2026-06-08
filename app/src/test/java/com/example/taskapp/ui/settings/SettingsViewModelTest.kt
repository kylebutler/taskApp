package com.example.taskapp.ui.settings

import app.cash.turbine.test
import com.example.taskapp.data.repository.settings.ThemePreference
import com.example.taskapp.data.repository.settings.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val repository = mockk<UserPreferencesRepository>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val themeFlow = MutableStateFlow(ThemePreference.OS)
    private val snoozeFlow = MutableStateFlow(5)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.themePreference } returns themeFlow
        every { repository.snoozeDuration } returns snoozeFlow
        viewModel = SettingsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `themePreference should emit value from repository`() = runTest {
        viewModel.themePreference.test {
            assertEquals(ThemePreference.OS, awaitItem())
            
            themeFlow.value = ThemePreference.DARK
            assertEquals(ThemePreference.DARK, awaitItem())
        }
    }

    @Test
    fun `setThemePreference should call repository`() = runTest {
        viewModel.setThemePreference(ThemePreference.LIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.setThemePreference(ThemePreference.LIGHT) }
    }

    @Test
    fun `snoozeDuration should emit value from repository`() = runTest {
        viewModel.snoozeDuration.test {
            assertEquals(5, awaitItem())
            
            snoozeFlow.value = 15
            assertEquals(15, awaitItem())
        }
    }

    @Test
    fun `setSnoozeDuration should call repository`() = runTest {
        viewModel.setSnoozeDuration(30)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.setSnoozeDuration(30) }
    }
}
