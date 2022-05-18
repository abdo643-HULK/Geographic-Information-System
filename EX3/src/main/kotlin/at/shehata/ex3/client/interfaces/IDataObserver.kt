package at.shehata.ex3.client.interfaces

import java.awt.Image

interface IDataObserver {
    fun update(_img: Image, _scale: Int)
}