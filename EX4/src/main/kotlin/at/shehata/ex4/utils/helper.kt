package at.shehata.ex4.utils

import java.lang.IllegalArgumentException


inline fun <reified T : Enum<T>> safeValueOf(type: String): T? {
    return try {
        java.lang.Enum.valueOf(T::class.java, type)
    } catch (_e: IllegalArgumentException) {
        return null
    }
}