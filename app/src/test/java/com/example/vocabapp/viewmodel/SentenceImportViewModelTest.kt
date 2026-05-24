package com.example.vocabapp.viewmodel

import com.example.vocabapp.data.repository.CustomImportRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SentenceImportViewModelTest {

    private lateinit var testDispatcher: TestDispatcher

    private fun buildViewModel(
        repository: CustomImportRepository = mockk {
            coEvery { remainingCustomContentCapacity() } returns 500
        }
    ) = SentenceImportViewModel(repository).also(::waitForInitialCapacity)

    private fun waitForInitialCapacity(vm: SentenceImportViewModel) {
        repeat(50) {
            if (vm.remainingCapacity.value != null) return
            Thread.sleep(10)
        }
    }

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no preview result or message`() = runTest(testDispatcher) {
        val vm = buildViewModel()
        assertNull(vm.preview.value)
        assertNull(vm.result.value)
        assertNull(vm.message.value)
        assertNull(vm.fileName.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `showLoading sets isLoading true and clears preview result and message`() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.showLoading()
        assertTrue(vm.isLoading.value)
        assertNull(vm.preview.value)
        assertNull(vm.result.value)
        assertNull(vm.message.value)
    }

    @Test
    fun `showMessage sets message and stops loading`() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.showLoading()
        vm.showMessage("読み込みに失敗しました")
        assertEquals("読み込みに失敗しました", vm.message.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `resetForNewFile clears all user-visible state`() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.showMessage("エラー")
        vm.resetForNewFile()
        assertNull(vm.preview.value)
        assertNull(vm.result.value)
        assertNull(vm.message.value)
        assertNull(vm.fileName.value)
    }

    @Test
    fun `showLoading then showMessage reflects final message state`() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.showLoading()
        assertTrue(vm.isLoading.value)
        vm.showMessage("ファイルの読み込みに失敗しました。形式とサイズを確認してください。")
        assertFalse(vm.isLoading.value)
        assertEquals("ファイルの読み込みに失敗しました。形式とサイズを確認してください。", vm.message.value)
    }

    @Test
    fun `multiple resetForNewFile calls are idempotent`() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.showMessage("エラー1")
        vm.resetForNewFile()
        vm.resetForNewFile()
        assertNull(vm.preview.value)
        assertNull(vm.result.value)
        assertNull(vm.message.value)
        assertNull(vm.fileName.value)
    }
}
