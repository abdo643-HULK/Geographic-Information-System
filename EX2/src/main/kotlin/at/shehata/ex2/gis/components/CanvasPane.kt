package at.shehata.ex2.gis.components

import at.shehata.ex2.GISApplication
import at.shehata.ex2.gis.GISController
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane

/**
 * CanvasPane component to separate view into smaller
 * pieces.
 * Creates a Canvas and sets the event listeners
 *
 * @param _mouseHandler MouseHandler for Dragging
 * @param _changeHandler ChangeHandler for the canvas size
 */
class CanvasPane(
    _mouseHandler: GISController.MouseHandler,
    _changeHandler: GISController.ChangeHandler,
) : StackPane() {
    init {
        setMinSize(0.0, 0.0)
        onMousePressed = _mouseHandler
        onMouseReleased = _mouseHandler
        val width = widthProperty()
        val height = heightProperty()
        width.addListener(_changeHandler)
        height.addListener(_changeHandler)
        val canvas = Canvas().apply {
            id = GISApplication.CANVAS_ID
            widthProperty().bind(width)
            heightProperty().bind(height)
        }
        children += canvas
    }
}