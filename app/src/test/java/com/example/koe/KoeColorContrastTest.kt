package com.example.koe

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.pow

/**
 * Guards the readability rules of the palette: any token change in koe_colors.xml that
 * drops a pair under WCAG AA fails the build.
 *
 * Reads the resource file itself because local unit tests have no Android resources;
 * Gradle runs them with the module directory as working directory.
 */
class KoeColorContrastTest {

    private val colors = parseColors(File("src/main/res/values/koe_colors.xml"))

    @Test
    fun everyColorPairMeetsWcagAa() {
        val roles = colors.keys
            .filter { it.endsWith(CONTAINER_SUFFIX) }
            .map { it.removeSuffix(CONTAINER_SUFFIX) }
        assertTrue("no color pair found", roles.isNotEmpty())
        roles.forEach { role ->
            assertReadable(role, color("$role$CONTENT_SUFFIX"), color("$role$CONTAINER_SUFFIX"))
        }
    }

    @Test
    fun everyTextColorIsReadableOnSurface() {
        val textRoles = colors.keys.filter { it.startsWith("koe_text_") && it != "koe_text_on_dark" }
        assertTrue("no text color found", textRoles.isNotEmpty())
        textRoles.forEach { role -> assertReadable(role, color(role), color("koe_surface")) }
    }

    @Test
    fun disabledTextIsReadableOnDisabledSurface() {
        assertReadable(
            role = "koe_text_disabled on koe_surface_disabled",
            foreground = color("koe_text_disabled"),
            background = color("koe_surface_disabled"),
        )
    }

    private fun assertReadable(role: String, foreground: Long, background: Long) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "$role: contrast ${"%.2f".format(ratio)}:1 is below $WCAG_AA_NORMAL_TEXT:1",
            ratio >= WCAG_AA_NORMAL_TEXT,
        )
    }

    /** Resolves `@color/` references down to an ARGB value. */
    private fun color(name: String): Long {
        val value = colors[name] ?: error("missing color $name")
        return if (value.startsWith("@color/")) color(value.removePrefix("@color/"))
        else value.removePrefix("#").toLong(16)
    }

    private fun contrastRatio(first: Long, second: Long): Double {
        val lighter = maxOf(luminance(first), luminance(second))
        val darker = minOf(luminance(first), luminance(second))
        return (lighter + RELATIVE_LUMINANCE_OFFSET) / (darker + RELATIVE_LUMINANCE_OFFSET)
    }

    private fun luminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val c = ((argb shr shift) and 0xFF) / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    private fun parseColors(file: File): Map<String, String> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(file).getElementsByTagName("color")
        return (0 until nodes.length).associate { i ->
            val node = nodes.item(i)
            node.attributes.getNamedItem("name").nodeValue to node.textContent.trim()
        }
    }

    private companion object {
        const val WCAG_AA_NORMAL_TEXT = 4.5
        const val RELATIVE_LUMINANCE_OFFSET = 0.05
        const val CONTAINER_SUFFIX = "_container"
        const val CONTENT_SUFFIX = "_content"
    }
}
