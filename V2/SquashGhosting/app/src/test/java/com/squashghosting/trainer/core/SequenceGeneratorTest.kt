package com.squashghosting.trainer.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SequenceGeneratorTest {

    @Test
    fun star_visitsEveryPositionOnce() {
        val gen = SequenceGenerator(Random(1))
        repeat(50) {
            val seq = gen.buildSequence(Pattern.STAR, 1)
            assertEquals(CourtPosition.ALL.toSet(), seq.toSet())
            assertEquals(6, seq.size)
        }
    }

    @Test
    fun rally_hasEightCallsAndNoImmediateRepeats() {
        val gen = SequenceGenerator(Random(2))
        repeat(100) {
            val seq = gen.buildSequence(Pattern.RANDOM_RALLY, 1)
            assertEquals(8, seq.size)
            seq.zipWithNext().forEach { (a, b) -> assertNotEquals(a, b) }
        }
    }

    @Test
    fun frontTwoCorners_isSixFrontCalls() {
        val gen = SequenceGenerator(Random(3))
        repeat(20) {
            val seq = gen.buildSequence(Pattern.FRONT_TWO_CORNERS, 1)
            assertEquals(6, seq.size)
            assertTrue(seq.all { it == CourtPosition.FRONT_LEFT || it == CourtPosition.FRONT_RIGHT })
        }
    }

    @Test
    fun diagonalHalfMoon_alternatesBySet() {
        val gen = SequenceGenerator(Random(4))
        assertEquals(8, gen.buildSequence(Pattern.DIAGONAL_HALF_MOON, 1).size) // diagonals
        assertEquals(6, gen.buildSequence(Pattern.DIAGONAL_HALF_MOON, 2).size) // half-moon
    }

    @Test
    fun nextSet_neverStartsWithPreviousLastCall() {
        val gen = SequenceGenerator(Random(5))
        for (pattern in Pattern.entries) {
            gen.reset()
            var last: CourtPosition? = null
            for (set in 1..200) {
                val seq = gen.nextSet(pattern, set)
                if (last != null) assertNotEquals(last, seq.first())
                last = seq.last()
                assertEquals(last, gen.lastPosition)
            }
        }
    }

    @Test
    fun positions_textAndMirror() {
        assertEquals("top right corner", CourtPosition.FRONT_RIGHT.spokenText)
        assertEquals("Bottom Left Corner", CourtPosition.BACK_LEFT.label)
        CourtPosition.entries.forEach { assertEquals(it, it.mirror.mirror) }
    }

    @Test
    fun voiceGender_fromName() {
        assertEquals(VoiceGender.MALE, VoiceNames.genderFromName("en-us-x-sfg#male_1-local"))
        assertEquals(VoiceGender.FEMALE, VoiceNames.genderFromName("en-us-x-iol#female_2-local"))
        assertEquals(VoiceGender.FEMALE, VoiceNames.genderFromName("en-gb-x-gba-network"))
        assertEquals(VoiceGender.MALE, VoiceNames.genderFromName("en-in-x-end-local"))
        assertEquals(VoiceGender.FEMALE, VoiceNames.genderFromName("Some Female Voice"))
        assertEquals(VoiceGender.MALE, VoiceNames.genderFromName("some-male"))
        assertNull(VoiceNames.genderFromName("en-us-x-unknown-local"))
    }
}
