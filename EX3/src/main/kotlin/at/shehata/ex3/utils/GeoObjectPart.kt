package at.shehata.ex3.utils

import java.awt.Graphics2D
import java.awt.geom.Area as AwtArea

abstract class GeoObjectPart(
	protected open val mHoles: List<GeoObjectPart> = mutableListOf()
) {
	abstract fun draw(_ctx: Graphics2D, _world: Matrix, _area: AwtArea)
}