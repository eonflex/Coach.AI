package com.coachai.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Coloured status badge pill.
 *
 * Used on the uploaded files list to indicate document processing state.
 *
 * @param label Text to display inside the badge
 * @param containerColor Background color of the badge
 */
@Composable
fun StatusBadge(
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Extraction-state badge variants using Material3 color scheme.
 * Backend field: DocumentResponse.extractionDone (Boolean) and chunkCount (Int).
 */
@Composable
fun ExtractionStatusBadge(extractionDone: Boolean, chunkCount: Int) {
    if (extractionDone) {
        StatusBadge(
            label = if (chunkCount > 0) "READY ($chunkCount chunks)" else "READY",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    } else {
        StatusBadge(
            label = "PROCESSING",
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

/**
 * Returns an appropriate Material icon for a given MIME type.
 *
 * Supported types: PDF, images, Word documents, spreadsheets, plain text.
 * Falls back to a generic file icon.
 */
@Composable
fun FileTypeIcon(contentType: String, modifier: Modifier = Modifier) {
    val icon: ImageVector = when {
        contentType.contains("pdf", ignoreCase = true)        -> Icons.Default.PictureAsPdf
        contentType.startsWith("image/")                      -> Icons.Default.Image
        contentType.contains("word", ignoreCase = true) ||
        contentType.contains("docx", ignoreCase = true) ||
        contentType.contains("document", ignoreCase = true)   -> Icons.Default.Description
        contentType.contains("spreadsheet", ignoreCase = true) ||
        contentType.contains("excel", ignoreCase = true) ||
        contentType.contains("xlsx", ignoreCase = true)       -> Icons.Default.TableChart
        contentType.startsWith("text/")                       -> Icons.Default.TextSnippet
        else                                                  -> Icons.Default.InsertDriveFile
    }
    Icon(icon, contentDescription = contentType, modifier = modifier)
}

/**
 * Scrollable row of source-document chips shown under an AI response.
 *
 * @param contextSummary The context summary string returned by the backend
 *                       (ChatResponse.contextSummary). Displayed verbatim in a
 *                       collapsible chip.
 */
@Composable
fun ContextSummaryRow(contextSummary: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = "Context used",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = contextSummary,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 2
        )
    }
}
