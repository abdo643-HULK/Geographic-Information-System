package at.shehata.ex3.utils

abstract class GeoObjectPart {
    private val mHoles = mutableListOf<GeoObjectPart>()
    abstract fun draw()
}