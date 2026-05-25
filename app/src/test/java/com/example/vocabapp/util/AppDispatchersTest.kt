package com.example.vocabapp.util

import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertSame
import org.junit.Test

class AppDispatchersTest {

    @Test
    fun `copy keeps main dispatcher when only io is overridden`() {
        val ioDispatcher = StandardTestDispatcher()
        val defaultDispatcher = StandardTestDispatcher()
        val mainDispatcher = StandardTestDispatcher()
        val replacementIoDispatcher = StandardTestDispatcher()

        val dispatchers = AppDispatchers(
            io = ioDispatcher,
            default = defaultDispatcher,
            main = mainDispatcher
        )

        val copied = dispatchers.copy(io = replacementIoDispatcher)

        assertSame(replacementIoDispatcher, copied.io)
        assertSame(defaultDispatcher, copied.default)
        assertSame(mainDispatcher, copied.main)
    }
}
