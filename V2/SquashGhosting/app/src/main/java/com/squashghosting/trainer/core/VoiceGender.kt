package com.squashghosting.trainer.core

/**
 * Voice texture.
 *
 * Big pitch shifts make any voice sound processed and "chipmunky". A real
 * male/female voice with only a small pitch/rate touch-up sounds far more
 * natural, and a slightly quicker rate gives the crisp, urgent delivery a
 * coach uses on court. [fallbackPitch] is used only when the engine has no
 * voice of the requested gender.
 */
enum class VoiceGender(
    val displayName: String,
    val key: String,
    val pitch: Float,
    val rate: Float,
    val fallbackPitch: Float,
) {
    FEMALE("Female", "female", pitch = 1.05f, rate = 1.12f, fallbackPitch = 1.18f),
    MALE("Male", "male", pitch = 0.92f, rate = 1.08f, fallbackPitch = 0.82f);

    companion object {
        val DEFAULT = FEMALE

        fun fromDisplayName(name: String): VoiceGender =
            entries.firstOrNull { it.displayName == name } ?: DEFAULT
    }
}

object VoiceNames {

    /**
     * Google TTS voice ids do not always contain the gender. Known ids
     * (name without the '-local' / '-network' suffix) -> gender.
     * Unknown voices still fall back to name matching ('#male_1', 'female', ...).
     */
    private val KNOWN_VOICE_GENDER: Map<String, VoiceGender> = buildMap {
        val f = VoiceGender.FEMALE
        val m = VoiceGender.MALE
        // en-US
        put("en-us-x-iob", f); put("en-us-x-iog", f); put("en-us-x-sfg", f)
        put("en-us-x-tpc", f); put("en-us-x-tpf", f)
        put("en-us-x-iol", m); put("en-us-x-iom", m); put("en-us-x-tpd", m)
        // en-GB
        put("en-gb-x-gba", f); put("en-gb-x-gbc", f); put("en-gb-x-gbg", f)
        put("en-gb-x-gbb", m); put("en-gb-x-gbd", m); put("en-gb-x-rjs", m)
        // en-IN
        put("en-in-x-ahp", f); put("en-in-x-cxx", f); put("en-in-x-ena", f)
        put("en-in-x-end", m)
        // en-AU
        put("en-au-x-afh", f); put("en-au-x-aua", f); put("en-au-x-auc", f)
        put("en-au-x-aub", m); put("en-au-x-aud", m)
    }

    /** Best-effort gender of an Android TTS voice from its name. */
    fun genderFromName(name: String): VoiceGender? {
        val n = name.lowercase()
        // Legacy Google variants: 'en-us-x-sfg#male_1-local' -> explicit gender
        if ("#female" in n) return VoiceGender.FEMALE
        if ("#male" in n) return VoiceGender.MALE

        var base = n.substringBefore('#')
        for (suffix in listOf("-local", "-network")) {
            if (base.endsWith(suffix)) base = base.removeSuffix(suffix)
        }
        KNOWN_VOICE_GENDER[base]?.let { return it }

        // Test 'female' first: 'male' is a substring of 'female'
        if ("female" in n) return VoiceGender.FEMALE
        if ("male" in n) return VoiceGender.MALE
        return null
    }
}
