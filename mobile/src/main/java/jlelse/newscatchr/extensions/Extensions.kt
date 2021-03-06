/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.extensions

import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.text.Html
import android.text.Spanned
import com.google.gson.Gson
import jlelse.newscatchr.appContext
import jlelse.newscatchr.backend.Feed
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory


fun InputStream.convertToString(): String? {
    var string: String? = null
    bufferedReader().let {
        string = it.readText()
        tryOrNull { it.close() }
    }
    return string
}

fun String.convertOpmlToFeeds() = tryOrNull {
    mutableListOf<Feed>().apply {
        SAXParserFactory.newInstance().newSAXParser().xmlReader.apply {
            contentHandler = object : DefaultHandler() {
                @Throws(SAXException::class)
                override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                    if (qName.equals("outline", ignoreCase = true) && attributes.getValue("xmlUrl") != null) {
                        add(Feed().apply {
                            title = attributes.getValue("title")
                            feedId = attributes.getValue("xmlUrl")
                            saved = true
                        })
                    }
                }
            }
            parse(InputSource(byteInputStream(charset("UTF-8"))))
        }
    }.toTypedArray()
}

fun Any.toJson(): String = Gson().toJson(this)

fun JSONObject.safeExtractString(name: String): String? = tryOrNull { getString(name) }

fun JSONObject.safeExtractJsonObject(name: String) = tryOrNull { getJSONObject(name) }

fun String.safeGetJsonObject(): JSONObject? = tryOrNull { JSONObject(this) }

fun String.buildExcerpt(words: Int) = split(" ").toMutableList().filter { it.notNullOrBlank() && it != "\n" }.take(words).joinToString(separator = " ", postfix = "...").trim()

fun String?.notNullOrBlank() = !isNullOrBlank()

fun <T> Array<out T>?.notNullAndEmpty() = this != null && isNotEmpty()

fun <T> Collection<T>?.notNullAndEmpty() = this != null && isNotEmpty()

fun Array<out String?>.removeBlankStrings() = mutableListOf<String>().apply { this@removeBlankStrings.filter { it.notNullOrBlank() }.forEach { add(it!!) } }.toTypedArray()

fun String.cleanHtml(): String? = if (notNullOrBlank()) Jsoup.clean(this, Whitelist.basic().addTags("h2", "h3", "h4", "h5", "h6")) else this

fun String.toHtml(): Spanned = if (android.os.Build.VERSION.SDK_INT < 24) {
    @Suppress("DEPRECATION")
    Html.fromHtml(this)
} else {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
}

fun <T> tryOrNull(code: () -> T): T? = try {
    code()
} catch(e: Exception) {
    e.printStackTrace()
    null
}

inline fun <reified T> Array<T>.turnAround() = mutableListOf<T>().apply { this@turnAround.forEach { add(0, it) } }.toTypedArray()

fun sharedPref() = PreferenceManager.getDefaultSharedPreferences(appContext)

fun Int.resStr() = tryOrNull { appContext?.resources?.getString(this) }

fun Int.resStrArr() = tryOrNull { appContext?.resources?.getStringArray(this) }

fun Int.resIntArr() = tryOrNull { appContext?.resources?.getIntArray(this) }

fun Int.resBool() = tryOrNull { appContext?.resources?.getBoolean(this) }

// fun Int.resDrw() = resDrw(null)

fun Int.resDrw(context: Context?, color: Int?) = tryOrNull {
    AppCompatResources.getDrawable(context ?: appContext!!, this)?.apply {
        if (color != null) DrawableCompat.setTint(this, color)
    }
}

fun Int.resClr(context: Context?) = tryOrNull { ContextCompat.getColor(context ?: appContext!!, this) }