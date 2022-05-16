package at.shehata.ex3.gis.components

import at.shehata.ex3.gis.GISController
import at.shehata.ex3.gis.GISView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox

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
    SCROLL_RIGHT,
    TOGGLE_POI
}

/**
 * BottomBar component to separate view into smaller
 * pieces.
 *
 * @param _actionHandler ActionHandler for the Buttons
 */
class BottomBar(
    _actionHandler: GISController.ActionHandler,
    _keyHandler: GISController.KeyHandler
) : HBox() {
    init {
        start(_actionHandler, _keyHandler)
    }

    /**
     * Creates the buttons and adds them to the BottomBar
     */
    private fun start(_actionHandler: GISController.ActionHandler, _keyHandler: GISController.KeyHandler) {
        style += "-fx-background-color: white; -fx-padding: 7"
        spacing = 7.0
        alignment = Pos.BOTTOM_LEFT

        val drawBtn = Button("Draw").apply {
            id = ButtonActions.DRAW.name
            onAction = _actionHandler
        }

        val zoomToFitBtn = Button("ZTF").apply {
            id = ButtonActions.ZOOM_TO_FIT.name
            onAction = _actionHandler
        }

        val zoomOutBtn = Button("-").apply {
            id = ButtonActions.ZOOM_OUT.name
            onAction = _actionHandler
        }

        val zoomInBtn = Button("+").apply {
            id = ButtonActions.ZOOM_IN.name
            onAction = _actionHandler
        }

        val moveBtns = GridPane().apply {
            hgap = 5.0
            vgap = 5.0
            style += "-fx-padding: 0 10;"

            val scrollLeftBtn = Button("L").apply {
                id = ButtonActions.SCROLL_LEFT.name
                onAction = _actionHandler
                add(this, 0, 1)
            }

            val scrollUpBtn = Button("U").apply {
                id = ButtonActions.SCROLL_UP.name
                onAction = _actionHandler
                add(this, 1, 0)
            }

            val scrollDownBtn = Button("D").apply {
                id = ButtonActions.SCROLL_DOWN.name
                onAction = _actionHandler
                add(this, 1, 1)
            }

            val scrollRightBtn = Button("R").apply {
                id = ButtonActions.SCROLL_RIGHT.name
                onAction = _actionHandler
                add(this, 2, 1)
            }
        }

        val scaleLabel = Label("1:").apply {
            padding = Insets(4.5, 0.0, 4.5, 0.0)
        }

        val scaleField = TextField("unknown").apply {
            id = GISView.SCALE_FIELD_ID
            padding = Insets(4.5, 0.0, 4.5, 2.0)
            onKeyReleased = _keyHandler
        }


        val poiBtn = Button("POI").apply {
            id = ButtonActions.TOGGLE_POI.name
            onAction = _actionHandler
        }

        children += arrayOf(
            drawBtn,
            zoomToFitBtn,
            zoomOutBtn,
            zoomInBtn,
            moveBtns,
            HBox().apply {
                alignment = Pos.BOTTOM_LEFT
                spacing = 2.0
                children += arrayOf(scaleLabel, scaleField)
            },
            poiBtn
        )
    }
}