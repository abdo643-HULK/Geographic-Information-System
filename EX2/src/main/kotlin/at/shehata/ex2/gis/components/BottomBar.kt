package at.shehata.ex2.gis.components

import at.shehata.ex2.gis.GISController
import javafx.scene.control.Button
import javafx.scene.layout.GridPane

enum class ButtonActions {
    DRAW,
    ZOOM_TO_FIT,
    ZOOM_IN,
    ZOOM_OUT,
    SCROLL_UP,
    SCROLL_DOWN,
    SCROLL_LEFT,
    SCROLL_RIGHT
}

class BottomBar(_handler: GISController.ActionHandler) : GridPane() {
    init {
        start(_handler)
    }

    fun start(_handler: GISController.ActionHandler) {
        style += "-fx-background-color: blue; -fx-padding: 10"

        val drawBtn = Button("Draw").apply {
            id = ButtonActions.DRAW.name
            style = "-fx-padding: 5"
            onAction = _handler
            add(this, 0, 1)
        }

        val zoomToFitBtn = Button("ZTF").apply {
            id = ButtonActions.ZOOM_TO_FIT.name
            onAction = _handler
            add(this, 1, 1)
        }

        val zoomOutBtn = Button("-").apply {
            id = ButtonActions.ZOOM_OUT.name
            onAction = _handler
            add(this, 3, 1)
        }

        val zoomInBtn = Button("+").apply {
            id = ButtonActions.ZOOM_IN.name
            onAction = _handler
            add(this, 4, 1)
        }

        val scrollUpBtn = Button("U").apply {
            id = ButtonActions.SCROLL_UP.name
            onAction = _handler
            add(this, 6, 0)
        }

        val scrollDownBtn = Button("D").apply {
            id = ButtonActions.SCROLL_DOWN.name
            onAction = _handler
            add(this, 6, 2)
        }

        val scrollRightBtn = Button("R").apply {
            id = ButtonActions.SCROLL_RIGHT.name
            onAction = _handler
            add(this, 7, 1)
        }

        val scrollLeftBtn = Button("L").apply {
            id = ButtonActions.SCROLL_LEFT.name
            onAction = _handler
            add(this, 5, 1)
        }
    }
}