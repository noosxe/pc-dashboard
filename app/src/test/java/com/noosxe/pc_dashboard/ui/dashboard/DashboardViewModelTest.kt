package com.noosxe.pc_dashboard.ui.dashboard

import app.cash.turbine.test
import com.noosxe.pc_dashboard.data.MediaState
import com.noosxe.pc_dashboard.data.PlayerState
import com.noosxe.pc_dashboard.data.PcNotification
import com.noosxe.pc_dashboard.data.PcRepository
import com.noosxe.pc_dashboard.data.PcStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shouldKeepScreenOn should emit false after 5 minutes of lock`() = runTest {
        val sessionLockFlow = MutableSharedFlow<Boolean>(replay = 1)
        sessionLockFlow.emit(false) // Start unlocked
        val repository = FakePcRepository(sessionLockFlow)
        val viewModel = DashboardViewModel(repository)

        viewModel.shouldKeepScreenOn.test {
            // Should start as true because host is unlocked
            assertEquals(true, awaitItem())

            // Emit locked = true
            sessionLockFlow.emit(true)
            // It should emit true again (starting the timer)
            // But StateFlow might not emit if value is same.
            // Wait, flatMapLatest will restart the flow.
            // If the inner flow emits 'true', and the StateFlow was already 'true', it might not emit a NEW item.
            
            // Let's check if it eventually emits false.
            advanceTimeBy(5 * 60 * 1000)
            assertEquals(false, awaitItem())
            
            // Unlock
            sessionLockFlow.emit(false)
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `mediaState should update when repository emits new media state`() = runTest {
        val mediaFlow = MutableSharedFlow<MediaState>(replay = 1)
        val repository = FakePcRepository(mediaStateFlow = mediaFlow)
        val viewModel = DashboardViewModel(repository)

        viewModel.mediaState.test {
            // Initial value should have no players (or mock players if using MockPcRepository, but here we use Fake)
            assertEquals(0, awaitItem().players.size)

            val newState = MediaState(players = listOf(PlayerState(player = "Spotify", title = "Song", artist = "Artist")))
            mediaFlow.emit(newState)
            val result = awaitItem()
            assertEquals(1, result.players.size)
            assertEquals("Spotify", result.players[0].player)
        }
    }

    private class FakePcRepository(
        private val sessionLockFlow: Flow<Boolean> = flowOf(false),
        private val mediaStateFlow: Flow<MediaState> = flowOf(MediaState())
    ) : PcRepository {
        override fun getPcStatsFlow(): Flow<PcStats> = flowOf(PcStats())
        override fun getNotificationsFlow(): Flow<PcNotification> = MutableSharedFlow()
        override fun getSessionLockFlow(): Flow<Boolean> = sessionLockFlow
        override fun getMediaStateFlow(): Flow<MediaState> = mediaStateFlow
        override fun sendMediaCommand(player: String, command: String) {}
    }
}
