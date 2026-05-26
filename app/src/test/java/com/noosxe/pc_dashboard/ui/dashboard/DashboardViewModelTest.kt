package com.noosxe.pc_dashboard.ui.dashboard

import app.cash.turbine.test
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

    private class FakePcRepository(
        private val sessionLockFlow: Flow<Boolean>
    ) : PcRepository {
        override fun getPcStatsFlow(): Flow<PcStats> = flowOf(PcStats())
        override fun getNotificationsFlow(): Flow<PcNotification> = MutableSharedFlow()
        override fun getSessionLockFlow(): Flow<Boolean> = sessionLockFlow
    }
}
