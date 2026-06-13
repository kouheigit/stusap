package com.example.vocabapp.ui

import com.example.vocabapp.ui.screen.common.gradeLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class GramFormatTest {
    @Test fun gradeBoundaries() {
        assertEquals("S", gradeLabel(100))
        assertEquals("A", gradeLabel(80))
        assertEquals("B", gradeLabel(60))
        assertEquals("C", gradeLabel(40))
        assertEquals("D", gradeLabel(0))
    }
}
