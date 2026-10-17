package com.squashghosting.trainer.core

enum class Pattern(val displayName: String) {
    FRONT_TWO_CORNERS("Front Two Corners"),
    BACKCOURT_SERVICE_BOX("Backcourt / Service Box"),
    DIAGONAL_HALF_MOON("Diagonal & Half-Moon"),
    RANDOM_RALLY("Random / Rally"),
    STAR("Star");

    companion object {
        val DEFAULT = RANDOM_RALLY

        fun fromDisplayName(name: String): Pattern =
            entries.firstOrNull { it.displayName == name } ?: DEFAULT
    }
}
