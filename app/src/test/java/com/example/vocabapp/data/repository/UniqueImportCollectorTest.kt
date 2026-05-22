package com.example.vocabapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniqueImportCollectorTest {

    private fun makeCollector(
        existing: Set<String> = emptySet(),
        availableSlots: Int = 100
    ): Pair<UniqueImportCollector<String>, ImportIssueCollector<String>> {
        val issues = ImportIssueCollector<String>()
        return UniqueImportCollector(existing, availableSlots, issues) to issues
    }

    @Test
    fun addIfUnique_addsNewItem() {
        val (collector, _) = makeCollector()
        collector.addIfUnique("hello", "Hello", rowNumber = 2, row = listOf("Hello"))
        assertEquals(listOf("Hello"), collector.newItems)
    }

    @Test
    fun addIfUnique_rejectsDuplicateFromExistingSet() {
        val (collector, issues) = makeCollector(existing = setOf("hello"))
        collector.addIfUnique("hello", "Hello", rowNumber = 2, row = listOf("Hello"))
        assertTrue(collector.newItems.isEmpty())
        assertEquals(listOf("Hello"), issues.duplicates)
    }

    @Test
    fun addIfUnique_rejectsDuplicateSeenWithinFile() {
        val (collector, issues) = makeCollector()
        collector.addIfUnique("hello", "Hello", rowNumber = 2, row = listOf("Hello"))
        collector.addIfUnique("hello", "Hello2", rowNumber = 3, row = listOf("Hello2"))
        assertEquals(listOf("Hello"), collector.newItems)
        assertEquals(listOf("Hello2"), issues.duplicates)
    }

    @Test
    fun addIfUnique_rejectsWhenCapacityExceeded() {
        val (collector, issues) = makeCollector(availableSlots = 1)
        collector.addIfUnique("first", "First", rowNumber = 2, row = listOf("First"))
        collector.addIfUnique("second", "Second", rowNumber = 3, row = listOf("Second"))
        assertEquals(listOf("First"), collector.newItems)
        assertEquals(1, issues.errors.size)
    }

    @Test
    fun newItems_reflectsAllSuccessfulAdditions() {
        val (collector, _) = makeCollector(existing = setOf("skip"))
        collector.addIfUnique("a", "A", rowNumber = 2, row = listOf("A"))
        collector.addIfUnique("skip", "Skip", rowNumber = 3, row = listOf("Skip"))
        collector.addIfUnique("b", "B", rowNumber = 4, row = listOf("B"))
        assertEquals(listOf("A", "B"), collector.newItems)
    }
}
