package com.example.koe.ui.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.koe.R

object KoeFonts {
    val Anton = FontFamily(Font(R.font.anton_regular))
    val Hanken = FontFamily(
        Font(R.font.hanken_grotesk_medium, FontWeight.Medium),
        Font(R.font.hanken_grotesk_semibold, FontWeight.SemiBold),
        Font(R.font.hanken_grotesk_bold, FontWeight.Bold),
        Font(R.font.hanken_grotesk_extrabold, FontWeight.ExtraBold),
    )
}

object KoeTextStyles {
    val Display = base(KoeFonts.Anton, FontWeight.Normal, 36.sp, 36.sp, 0.sp)
    val DieNumber = base(KoeFonts.Anton, FontWeight.Normal, 30.sp, 30.sp, 0.sp)
    val ButtonLarge = base(KoeFonts.Anton, FontWeight.Normal, 24.sp, 26.sp, 0.5.sp)
    val StatNumber = base(KoeFonts.Anton, FontWeight.Normal, 16.sp, TextUnit.Unspecified, 0.sp)

    val Button = base(KoeFonts.Hanken, FontWeight.ExtraBold, 16.sp, 20.sp, 0.3.sp)
    val BadgeCount = base(KoeFonts.Hanken, FontWeight.ExtraBold, 13.sp, 18.sp, 0.3.sp)
    val Name = base(KoeFonts.Hanken, FontWeight.Bold, 15.sp, 20.sp, 0.sp)
    val ChipStat = base(KoeFonts.Hanken, FontWeight.ExtraBold, 15.sp, 20.sp, 0.sp)
    // Figma uses Black 900; no Black file ships with the app, so ExtraBold is the closest.
    val KingTitle = base(KoeFonts.Hanken, FontWeight.ExtraBold, 14.sp, 28.sp, 0.sp)
    val Subtitle = base(KoeFonts.Hanken, FontWeight.SemiBold, 13.sp, 18.sp, 0.sp)
    val Pill = base(KoeFonts.Hanken, FontWeight.Bold, 15.sp, 20.sp, 0.sp)
    val Caption = base(KoeFonts.Hanken, FontWeight.ExtraBold, 12.sp, 14.sp, 0.6.sp)
    val Description = base(KoeFonts.Hanken, FontWeight.SemiBold, 12.sp, 16.sp, 0.sp)
    val StatLabel = base(KoeFonts.Hanken, FontWeight.Bold, 12.sp, 18.sp, 0.sp)
    val StatLabelLarge = base(KoeFonts.Hanken, FontWeight.Bold, 13.sp, 18.sp, 0.sp)
    val StatLabelTight = base(KoeFonts.Hanken, FontWeight.Bold, 12.sp, TextUnit.Unspecified, 0.sp)
    val StatNumberUi = base(KoeFonts.Hanken, FontWeight.ExtraBold, 16.sp, TextUnit.Unspecified, 0.sp)

    val DialogTitle = base(KoeFonts.Anton, FontWeight.Normal, 36.sp, 40.sp, 0.sp)
    val DialogTitleSmall = base(KoeFonts.Anton, FontWeight.Normal, 30.sp, 36.sp, 0.sp)
    val SheetTitle = base(KoeFonts.Anton, FontWeight.Normal, 26.sp, 28.sp, 0.sp)
    val PlayerName = base(KoeFonts.Anton, FontWeight.Normal, 28.sp, 36.sp, 0.sp)
    val Body = base(KoeFonts.Hanken, FontWeight.SemiBold, 15.sp, 20.sp, 0.sp)
    val BodyStrong = base(KoeFonts.Hanken, FontWeight.ExtraBold, 15.sp, 20.sp, 0.sp)
    val SectionTitle = base(KoeFonts.Hanken, FontWeight.ExtraBold, 20.sp, 24.sp, 0.sp)
    val CardTitle = base(KoeFonts.Hanken, FontWeight.ExtraBold, 13.sp, 18.sp, 0.sp)
    val CardBadge = base(KoeFonts.Hanken, FontWeight.ExtraBold, 10.sp, 14.sp, 0.6.sp)
    val Value = base(KoeFonts.Hanken, FontWeight.ExtraBold, 16.sp, 20.sp, 0.sp)
    val ValueSpaced = base(KoeFonts.Hanken, FontWeight.ExtraBold, 15.sp, 20.sp, 0.3.sp)

    private fun base(
        family: FontFamily,
        weight: FontWeight,
        size: TextUnit,
        lineHeight: TextUnit,
        letterSpacing: TextUnit,
    ) = TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = size,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    )
}
