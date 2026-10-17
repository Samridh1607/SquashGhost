package com.squashghosting.trainer.core

import com.squashghosting.trainer.core.CourtPosition.BACK_LEFT
import com.squashghosting.trainer.core.CourtPosition.BACK_RIGHT
import com.squashghosting.trainer.core.CourtPosition.FRONT_LEFT
import com.squashghosting.trainer.core.CourtPosition.FRONT_RIGHT
import com.squashghosting.trainer.core.CourtPosition.LEFT
import com.squashghosting.trainer.core.CourtPosition.RIGHT
import kotlin.random.Random

/**
 * Builds the list of calls for each set. Stateful: remembers the last call of
 * the previous set so the next set never starts with the same position.
 */
class SequenceGenerator(private val random: Random = Random.Default) {

    /** Last call of the previous set. */
    var lastPosition: CourtPosition? = null
        private set

    fun reset() {
        lastPosition = null
    }

    /**
     * Return the list of positions for one set, according to the pattern.
     * No pattern calls the T: the player recovers to the T after every call.
     */
    fun buildSequence(pattern: Pattern, setNumber: Int): List<CourtPosition> {
        when (pattern) {
            Pattern.STAR ->
                // Every position visited once, in random order (T -> pos -> T implied)
                return CourtPosition.ALL.shuffled(random)

            Pattern.RANDOM_RALLY -> {
                // Rally-like: random walk, never the same call twice in a row
                val seq = ArrayList<CourtPosition>(RALLY_LENGTH)
                var prev: CourtPosition? = null
                repeat(RALLY_LENGTH) {
                    val choice = CourtPosition.ALL.filter { it != prev }.random(random)
                    seq += choice
                    prev = choice
                }
                return seq
            }

            else -> Unit
        }

        var seq: List<CourtPosition> = when (pattern) {
            Pattern.FRONT_TWO_CORNERS -> repeatList(listOf(FRONT_LEFT, FRONT_RIGHT), 3)
            Pattern.BACKCOURT_SERVICE_BOX -> repeatList(listOf(BACK_LEFT, LEFT, BACK_RIGHT, RIGHT), 2)
            // 'Diagonal & Half-Moon': alternate per set
            else -> if (setNumber % 2 == 1) {
                repeatList(listOf(FRONT_LEFT, BACK_RIGHT, FRONT_RIGHT, BACK_LEFT), 2) // diagonals
            } else {
                listOf(FRONT_LEFT, LEFT, BACK_LEFT, BACK_RIGHT, RIGHT, FRONT_RIGHT) // half-moon arc
            }
        }

        // Randomly mirror so the player doesn't always start on the same side
        if (random.nextDouble() < 0.5) {
            seq = seq.map { it.mirror }
        }
        return seq
    }

    /** Build a sequence whose first call differs from the previous set's last. */
    fun nextSet(pattern: Pattern, setNumber: Int): List<CourtPosition> {
        var seq: List<CourtPosition> = emptyList()
        var accepted = false
        for (attempt in 0 until MAX_ATTEMPTS) {
            seq = buildSequence(pattern, setNumber)
            if (lastPosition == null || seq.first() != lastPosition) {
                accepted = true
                break
            }
        }
        if (!accepted) {
            // Extremely unlikely fallback: rotate to the first different position
            val idx = seq.indexOfFirst { it != lastPosition }.coerceAtLeast(0)
            seq = seq.drop(idx) + seq.take(idx)
        }
        lastPosition = seq.last()
        return seq
    }

    private fun repeatList(list: List<CourtPosition>, times: Int): List<CourtPosition> =
        List(times) { list }.flatten()

    companion object {
        const val RALLY_LENGTH = 8
        private const val MAX_ATTEMPTS = 10
    }
}
