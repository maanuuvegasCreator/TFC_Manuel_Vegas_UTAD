package com.example.imdbapplogin.utils

fun String.cleanHtmlEntities(): String {
    return this
        .replace("&quot;", "\"")
        .replace("&#039;", "'")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&eacute;", "é")
        .replace("&uuml;", "ü")
        .replace("&ldquo;", "“")
        .replace("&rdquo;", "”")
        .replace("&lsquo;", "‘")
        .replace("&rsquo;", "’")
        .replace("&hellip;", "…")
        .replace("&mdash;", "—")
        .replace("&ndash;", "–")
}
