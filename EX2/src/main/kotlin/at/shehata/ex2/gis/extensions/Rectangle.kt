package at.shehata.ex2.gis.extensions

import java.awt.Rectangle

operator fun Rectangle.plus(_other: String): String = toString() + _other