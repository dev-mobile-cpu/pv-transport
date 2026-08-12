package com.pv.transport.extension

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class ThousandSeparatorTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {

        val originalText = text.text

        if (originalText.isEmpty()) {
            return TransformedText(
                text,
                OffsetMapping.Identity
            )
        }

        val formattedText = try {
            NumberFormat
                .getNumberInstance(Locale.US)
                .format(originalText.toLong())
        } catch (e: Exception) {
            originalText
        }


        return TransformedText(
            text = AnnotatedString(formattedText),
            offsetMapping = ThousandSeparatorOffsetMapping(
                originalText,
                formattedText
            )
        )
    }
}


private class ThousandSeparatorOffsetMapping(
    private val original: String,
    private val formatted: String
) : OffsetMapping {


    override fun originalToTransformed(offset: Int): Int {

        var originalIndex = 0
        var transformedIndex = 0


        while (
            originalIndex < offset &&
            transformedIndex < formatted.length
        ) {

            if (formatted[transformedIndex].isDigit()) {
                originalIndex++
            }

            transformedIndex++
        }

        return transformedIndex
    }


    override fun transformedToOriginal(offset: Int): Int {

        var originalIndex = 0
        var transformedIndex = 0


        while (
            transformedIndex < offset &&
            transformedIndex < formatted.length
        ) {

            if (formatted[transformedIndex].isDigit()) {
                originalIndex++
            }

            transformedIndex++
        }

        return originalIndex
    }
}