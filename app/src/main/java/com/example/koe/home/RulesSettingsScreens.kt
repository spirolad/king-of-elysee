package com.example.koe.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.ui.components.KoeButton
import com.example.koe.ui.components.KoeButtonStyle
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.theme.KoeTextStyles

/** 04 · Règles: four cards, one per step of a turn. */
@Composable
fun RulesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.koe_screen_home_container))
            .padding(rememberScreenPadding()),
    ) {
        KoeScreenHeader(title = stringResource(R.string.rules_title), onBack = onBack) {
            KoeButton(
                text = stringResource(R.string.btn_understood),
                onClick = onBack,
                style = KoeButtonStyle.Primary,
                leadingIcon = R.drawable.ic_dialog_check,
                iconSize = 20.dp,
                modifier = Modifier
                    .height(52.dp)
                    .width(158.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RuleCard(stringResource(R.string.rules_roll_title)) {
                RuleText(stringResource(R.string.rules_roll_body), stringResource(R.string.rules_roll_hint))
            }
            RuleCard(stringResource(R.string.rules_resolve_title)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RuleLine(lead = {
                        KoeText(
                            text = stringResource(R.string.rules_resolve_triple_mark),
                            style = KoeTextStyles.Multiplier,
                            color = colorResource(R.color.koe_text_votes),
                            softWrap = false,
                        )
                    }, text = stringResource(R.string.rules_resolve_triple))
                    RuleIconLine(R.drawable.ic_board_stat_popularite, stringResource(R.string.rules_resolve_popularite))
                    RuleIconLine(R.drawable.ic_board_stat_fonds, stringResource(R.string.rules_resolve_fonds))
                    RuleIconLine(R.drawable.ic_home_tweet, stringResource(R.string.rules_resolve_tweet))
                }
            }
            RuleCard(stringResource(R.string.rules_elysee_title)) {
                RuleText(stringResource(R.string.rules_elysee_body), stringResource(R.string.rules_elysee_hint))
            }
            RuleCard(stringResource(R.string.rules_win_title)) {
                RuleText(stringResource(R.string.rules_win_body), stringResource(R.string.rules_win_hint))
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.RuleCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(colorResource(R.color.koe_surface), RoundedCornerShape(24.dp))
            // Small phones are shorter than Figma's frame: the card scrolls instead of clipping.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        KoeText(
            text = title,
            style = KoeTextStyles.CardHeading,
            color = colorResource(R.color.koe_text_primary),
            softWrap = false,
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun RuleText(body: String, hint: String) {
    KoeText(text = body, style = KoeTextStyles.BodySmall, color = colorResource(R.color.koe_text_primary))
    Spacer(Modifier.height(10.dp))
    KoeText(text = hint, style = KoeTextStyles.Subtitle, color = colorResource(R.color.koe_text_muted))
}

@Composable
private fun RuleIconLine(@DrawableRes icon: Int, text: String) = RuleLine(
    lead = { Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(21.dp)) },
    text = text,
)

@Composable
private fun RuleLine(lead: @Composable () -> Unit, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(21.dp), contentAlignment = Alignment.CenterStart) { lead() }
        Spacer(Modifier.width(10.dp))
        KoeText(text = text, style = KoeTextStyles.Small, color = colorResource(R.color.koe_text_primary))
    }
}

/** 05 · Réglages. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onRules: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.koe_screen_home_container))
            .padding(rememberScreenPadding()),
    ) {
        KoeScreenHeader(title = stringResource(R.string.settings_title), onBack = onBack)
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorResource(R.color.koe_surface), RoundedCornerShape(24.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            ) {
                SettingRow(KoeSetting.SOUNDS, R.string.settings_sounds, R.string.settings_sounds_hint, divider = true)
                SettingRow(KoeSetting.MUSIC, R.string.settings_music, R.string.settings_music_hint, divider = true)
                SettingRow(
                    KoeSetting.VIBRATIONS,
                    R.string.settings_vibrations,
                    R.string.settings_vibrations_hint,
                    divider = true,
                )
                SettingRow(
                    KoeSetting.REDUCED_MOTION,
                    R.string.settings_reduced_motion,
                    R.string.settings_reduced_motion_hint,
                    divider = false,
                )
            }
            Column(
                Modifier
                    .width(260.dp)
                    .fillMaxHeight(),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.koe_surface), RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    KoeText(
                        text = stringResource(R.string.settings_language),
                        style = KoeTextStyles.Caption,
                        color = colorResource(R.color.koe_text_muted),
                    )
                    Spacer(Modifier.height(6.dp))
                    KoeText(
                        text = stringResource(R.string.settings_language_value),
                        style = KoeTextStyles.Name,
                        color = colorResource(R.color.koe_text_primary),
                    )
                }
                Spacer(Modifier.height(12.dp))
                KoeButton(
                    text = stringResource(R.string.btn_review_rules),
                    onClick = onRules,
                    style = KoeButtonStyle.Secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                )
                Spacer(Modifier.weight(1f))
                KoeText(
                    text = stringResource(R.string.settings_disclaimer),
                    style = KoeTextStyles.Subtitle,
                    color = colorResource(R.color.koe_screen_home_content),
                    modifier = Modifier.padding(end = 40.dp, bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingRow(setting: KoeSetting, title: Int, hint: Int, divider: Boolean) {
    var checked by rememberSetting(setting)
    val line = colorResource(R.color.koe_action_outline)
    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = { checked = it })
            .then(
                if (divider) {
                    Modifier.drawBehind {
                        val y = size.height - 1.dp.toPx()
                        drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
                    }
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            KoeText(stringResource(title), KoeTextStyles.Name, colorResource(R.color.koe_text_primary))
            KoeText(stringResource(hint), KoeTextStyles.Subtitle, colorResource(R.color.koe_text_muted))
        }
        KoeSwitch(checked)
    }
}

/** 60 x 36 switch: ink knob on azur when on, white knob on mist when off. */
@Composable
private fun KoeSwitch(checked: Boolean) {
    val track by animateColorAsState(
        if (checked) colorResource(R.color.koe_screen_home_container) else colorResource(R.color.koe_surface_sunken),
        label = "switchTrack",
    )
    val knob: Color by animateColorAsState(
        if (checked) colorResource(R.color.koe_action_primary_container) else colorResource(R.color.koe_surface),
        label = "switchKnob",
    )
    val knobX by animateDpAsState(if (checked) 28.dp else 4.dp, label = "switchKnobX")
    Box(
        Modifier
            .size(60.dp, 36.dp)
            .background(track, RoundedCornerShape(18.dp)),
    ) {
        Box(
            Modifier
                .offset(x = knobX, y = 4.dp)
                .size(28.dp)
                .background(knob, CircleShape),
        )
    }
}

@Preview(widthDp = 844, heightDp = 390)
@Composable
private fun RulesScreenPreview() = RulesScreen(onBack = {})

@Preview(widthDp = 844, heightDp = 390)
@Composable
private fun SettingsScreenPreview() = SettingsScreen(onBack = {}, onRules = {})
