package at.shehata.ex2.gis

import at.shehata.ex2.gis.components.ButtonActions
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.awt.Point
import kotlin.math.PI


/**
 * contains the core logic that binds view and model
 *
 * @param mModel The model that contains the business logic
 */
class GISController(private val mModel: GISModel) {
    val scope = CoroutineScope(Dispatchers.Default)

    /**
     * the singletons of the different handlers
     */
    private val mActionHandler by lazy { ActionHandler() }
    private val mChangeHandler by lazy { ChangeHandler() }
    private val mMouseHandler by lazy { MouseHandler() }
    private val mKeyHandler by lazy { KeyHandler() }

    /**
     * getters of the handler singletons
     */
    fun getActionHandler() = mActionHandler
    fun getMouseHandler() = mMouseHandler
    fun getChangeHandler() = mChangeHandler
    fun getKeyHandler() = mKeyHandler

    /**
     * handler for the button
     * calls the model to generate a house on a random position
     */
    inner class ActionHandler : EventHandler<ActionEvent> {
        @Suppress("MagicNumber")
        override fun handle(_event: ActionEvent): Unit = when (ButtonActions.valueOf((_event.source as Button).id)) {
            ButtonActions.DRAW -> {
                scope.launch { mModel.drawData() }
                Unit
            }
            ButtonActions.ZOOM_TO_FIT -> {
                scope.launch { mModel.zoomToFitNonBlock() }
                Unit
            }
            ButtonActions.ZOOM_IN -> mModel.zoom(1.3)
            ButtonActions.ZOOM_OUT -> mModel.zoom(1 / 1.3)
            ButtonActions.SCROLL_UP -> mModel.scrollVertical(20)
            ButtonActions.SCROLL_DOWN -> mModel.scrollVertical(-20)
            ButtonActions.SCROLL_LEFT -> mModel.scrollHorizontal(20)
            ButtonActions.SCROLL_RIGHT -> mModel.scrollHorizontal(-20)
        }
    }

    /**
     * handler for mouse clicks on the canvas
     * calls the model to render a house on the position off the click
     */
    inner class MouseHandler : EventHandler<MouseEvent> {
        private val mStartPoint = Point()
        override fun handle(_event: MouseEvent) {
            when (_event.eventType) {
                MouseEvent.MOUSE_PRESSED -> mStartPoint.setLocation(_event.x.toInt(), _event.y.toInt())
                MouseEvent.MOUSE_RELEASED -> {
                    mModel.scrollHorizontal(_event.x.toInt() - mStartPoint.x)
                    mModel.scrollVertical(_event.y.toInt() - mStartPoint.y)
                }
            }
        }
    }

    inner class KeyHandler : EventHandler<KeyEvent> {
        override fun handle(_event: KeyEvent) {
            when (_event.eventType) {
                KeyEvent.KEY_RELEASED -> when (_event.code) {
                    KeyCode.R -> mModel.rotate(PI / 2)
                    else -> return
                }
                KeyEvent.KEY_PRESSED -> handleKeyPress(_event.code)
            }
        }

        private fun handleKeyPress(_eventCode: KeyCode) {
            when (_eventCode) {
                KeyCode.UP -> mModel.scrollVertical(20)
                KeyCode.DOWN -> mModel.scrollVertical(-20)
                KeyCode.LEFT -> mModel.scrollHorizontal(20)
                KeyCode.RIGHT -> mModel.scrollHorizontal(-20)
                KeyCode.W -> mModel.scrollVertical(20)
                KeyCode.S -> mModel.scrollVertical(-20)
                KeyCode.A -> mModel.scrollHorizontal(20)
                KeyCode.D -> mModel.scrollHorizontal(-20)
                else -> return
            }
        }
    }

    /**
     * handler that listens for resizes of the canvas
     */
    inner class ChangeHandler : ChangeListener<Number> {
        override fun changed(_observable: ObservableValue<out Number>, _oldValue: Number, _newValue: Number) {
            when ((_observable as ReadOnlyDoubleProperty).name) {
                "width" -> mModel.setWidth(_newValue.toInt())
                "height" -> mModel.setHeight(_newValue.toInt())
            }
        }
    }
}