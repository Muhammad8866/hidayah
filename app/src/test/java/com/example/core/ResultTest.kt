package com.example.core

import com.example.core.result.Result
import org.junit.Assert.*
import org.junit.Test

class ResultTest {

    @Test
    fun testSuccessOperations() {
        val success: Result<Int, String> = Result.Success(42)
        assertTrue(success.isSuccess)
        assertFalse(success.isFailure)
        assertEquals(42, success.getOrNull())
        assertNull(success.errorOrNull())

        val mapped = success.map { it * 2 }
        assertEquals(84, mapped.getOrNull())

        val flatMapped = success.flatMap { Result.Success("Number: $it") }
        assertEquals("Number: 42", flatMapped.getOrNull())
    }

    @Test
    fun testFailureOperations() {
        val failure: Result<Int, String> = Result.Failure("Computation error")
        assertFalse(failure.isSuccess)
        assertTrue(failure.isFailure)
        assertNull(failure.getOrNull())
        assertEquals("Computation error", failure.errorOrNull())

        val mapped = failure.map { it * 2 }
        assertTrue(mapped.isFailure)
        assertEquals("Computation error", mapped.errorOrNull())
    }
}
