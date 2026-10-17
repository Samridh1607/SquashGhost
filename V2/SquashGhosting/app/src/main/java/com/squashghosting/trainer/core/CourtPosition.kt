package com.squashghosting.trainer.core

/**
 * Court positions. ASSUMPTION: "top" = front of the court, "bottom" = back.
 * LEFT / RIGHT = mid-court (service box) sides.
 *
 * The T is never called. Recovering to the T after every movement is implied
 * by every call (that is how real ghosting works), so the delay between
 * instructions is the "move + recover to T" window.
 */
enum class CourtPosition(val id: String) {
    LEFT("left"),
    RIGHT("right"),
    FRONT_LEFT("top-left-corner"),
    BACK_LEFT("bottom-left-corner"),
    FRONT_RIGHT("top-right-corner"),
    BACK_RIGHT("bottom-right-corner");

    /** Text handed to the TTS engine ("top right corner"). */
    val spokenText: String get() = id.replace('-', ' ')

    /** Text shown in the progress line ("Top Right Corner"). */
    val label: String
        get() = id.split('-').joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        }

    val mirror: CourtPosition
        get() = when (this) {
            LEFT -> RIGHT
            RIGHT -> LEFT
            FRONT_LEFT -> FRONT_RIGHT
            FRONT_RIGHT -> FRONT_LEFT
            BACK_LEFT -> BACK_RIGHT
            BACK_RIGHT -> BACK_LEFT
        }

    companion object {
        /** Same order as POSITIONS in the original Python app. */
        val ALL: List<CourtPosition> =
            listOf(LEFT, RIGHT, FRONT_LEFT, BACK_LEFT, FRONT_RIGHT, BACK_RIGHT)
    }
}
