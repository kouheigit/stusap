package com.example.vocabapp

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

internal fun parseWorkbookSheetPath(relsBytes: ByteArray?): String? {
    if (relsBytes == null) {
        debugImportLog("parseWorkbookSheetPath: no workbook.xml.rels found, using default")
        return null
    }
    return try {
        val parser = newXmlParser(relsBytes)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                val localName = parser.name.substringAfterLast(':')
                if (localName == "Relationship") {
                    val type = parser.getAttributeValue(null, "Type").orEmpty()
                    val target = parser.getAttributeValue(null, "Target").orEmpty()
                    debugImportLog("parseWorkbookSheetPath: Relationship type=$type target=$target")
                    if (type.endsWith("/worksheet") && target.isNotBlank()) {
                        val resolved = if (target.startsWith("/")) {
                            target.trimStart('/')
                        } else {
                            "xl/$target"
                        }
                        debugImportLog("parseWorkbookSheetPath: resolved sheet path=$resolved")
                        return resolved
                    }
                }
            }
        }
        warnImportLog("parseWorkbookSheetPath: no worksheet relationship found in workbook.xml.rels")
        null
    } catch (e: Exception) {
        warnImportLog("parseWorkbookSheetPath: failed to parse workbook relationships: ${e.javaClass.simpleName}: ${e.message}")
        null
    }
}

internal fun newXmlParser(bytes: ByteArray): XmlPullParser {
    val factory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = false
    }
    return factory.newPullParser().apply {
        setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        setInput(InputStreamReader(ByteArrayInputStream(bytes), StandardCharsets.UTF_8))
    }
}
