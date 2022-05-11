package at.shehata.ex3.gis.components

import at.shehata.ex3.gis.GISController
import javafx.scene.control.Button
import javafx.scene.layout.GridPane

/**
 * The supported Actions of the Buttons in
 * the BottomBar of the App
 */
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

/**
 * BottomBar component to separate view into smaller
 * pieces.
 *
 * @param _handler ActionHandler for the Buttons
 */
class BottomBar(_handler: GISController.ActionHandler) : GridPane() {
    init {
        start(_handler)
    }

    /**
     * Creates the buttons and adds them to the BottomBar
     */
    private fun start(_handler: GISController.ActionHandler) {
        style += "-fx-background-color: white; -fx-padding: 7"
        hgap = 7.0

        val drawBtn = Button("Draw").apply {
            id = ButtonActions.DRAW.name
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