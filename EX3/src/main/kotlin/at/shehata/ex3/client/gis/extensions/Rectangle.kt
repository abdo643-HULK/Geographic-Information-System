package at.shehata.ex3.client.gis.extensions

import java.awt.Rectangle

/**
 * extension to allow string concatenation on the Rectangle Class
 */
operator fun Rectangle.plus(_other: String): String = toString() + _other

