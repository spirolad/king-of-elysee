package com.example.koe.home

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

/** The player's preferences of the RÉGLAGES screen, kept across launches. */
enum class KoeSetting(val key: String, val default: Boolean) {
    SOUNDS("sounds", true),
    MUSIC("music", false),
    VIBRATIONS("vibrations", true),
    REDUCED_MOTION("reduced_motion", false),
}

private const val PREFS = "koe_settings"

/** Reads [setting]; writing the returned state saves it. */
@Composable
fun rememberSetting(setting: KoeSetting): MutableState<Boolean> {
    val prefs = LocalContext.current.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return remember(setting) { SavedSetting(prefs, setting) }
}

private class SavedSetting(
    private val prefs: android.content.SharedPreferences,
    private val setting: KoeSetting,
) : MutableState<Boolean> {
    private val state = mutableStateOf(prefs.getBoolean(setting.key, setting.default))

    override var value: Boolean
        get() = state.value
        set(value) {
            state.value = value
            prefs.edit { putBoolean(setting.key, value) }
        }

    override fun component1(): Boolean = value

    override fun component2(): (Boolean) -> Unit = { value = it }
}
