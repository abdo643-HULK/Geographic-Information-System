package at.shehata.ex4.ui.components

import at.shehata.ex4.GISApplication
import javafx.scene.canvas.Canvas
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane

/**
 * CanvasPane component to separate view into smaller
 * pieces.
 * Creates a Canvas and sets the event listeners
 *
 * @param _mouseHandler MouseHandler for Dragging
 * @param _changeHandler ChangeHandler for the canvas size
 */
class CanvasPane : HBox() {
    init {
        setMinSize(0.0, 0.0)
        children += arrayOf(SatView(), DeviationView())
    }
}