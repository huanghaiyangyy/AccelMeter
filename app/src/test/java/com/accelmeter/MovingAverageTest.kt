package com.accelmeter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.LinkedList

/**
 * Unit tests for the moving average filter used in MainActivity.
 */
class MovingAverageTest {

    private val filterSize = 15

    private fun movingAverage(buffer: LinkedList<Float>, newValue: Float): Float {
        buffer.addLast(newValue)
        if (buffer.size > filterSize) {
            buffer.removeFirst()
        }
        return buffer.sum() / buffer.size
    }

    @Test
    fun `single value returns itself`() {
        val buffer = LinkedList<Float>()
        val result = movingAverage(buffer, 3.5f)
        assertEquals(3.5f, result, 0.001f)
    }

    @Test
    fun `average of fewer than filterSize values is correct`() {
        val buffer = LinkedList<Float>()
        movingAverage(buffer, 1f)
        movingAverage(buffer, 2f)
        val result = movingAverage(buffer, 3f)
        // average of 1, 2, 3 = 2.0
        assertEquals(2.0f, result, 0.001f)
    }

    @Test
    fun `window slides correctly after exceeding filterSize`() {
        val buffer = LinkedList<Float>()
        // Fill buffer with 15 values of 0.0
        repeat(filterSize) { movingAverage(buffer, 0f) }
        // Add one value of 15.0; oldest 0.0 is evicted, average = 15/15 = 1.0
        val result = movingAverage(buffer, 15f)
        assertEquals(1.0f, result, 0.001f)
    }

    @Test
    fun `buffer does not exceed filterSize`() {
        val buffer = LinkedList<Float>()
        repeat(filterSize + 5) { movingAverage(buffer, it.toFloat()) }
        assertEquals(filterSize, buffer.size)
    }

    @Test
    fun `steady signal returns the signal value`() {
        val buffer = LinkedList<Float>()
        repeat(20) { movingAverage(buffer, 9.8f) }
        val result = movingAverage(buffer, 9.8f)
        assertEquals(9.8f, result, 0.001f)
    }
}
