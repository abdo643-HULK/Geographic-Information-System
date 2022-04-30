package at.shehata.ex2.gis

import at.shehata.ex2.gis.components.ButtonActions
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent


/**
 * contains the core logic that binds view and model
 *
 * @param mModel The model that contains the business logic
 */
class GISController(private val mModel: GISModel) {
    /**
     * the singletons of the different handlers
     */
    private val mActionHandler by lazy { ActionHandler() }
    private val mChangeHandler by lazy { ChangeHandler() }
    private val mMouseHandler by lazy { MouseHandler() }

    /**
     * getters of the handler singletons
     */
    fun getActionHandler() = mActionHandler
    fun getMouseHandler() = mMouseHandler
    fun getChangeHandler() = mChangeHandler

    /**
     * handler for the button
     * calls the model to generate a house on a random position
     */
    inner class ActionHandler : EventHandler<ActionEvent> {
        @Suppress("MagicNumber")
        override fun handle(_event: ActionEvent) = when (ButtonActions.valueOf((_event.source as Button).id)) {
            ButtonActions.DRAW -> mModel.generateRndHome()
            ButtonActions.ZOOM_TO_FIT -> mModel.zoomToFit()
            ButtonActions.ZOOM_IN -> mModel.zoom(1.3)
            ButtonActions.ZOOM_OUT -> mModel.zoom(1 / 1.3)
            ButtonActions.SCROLL_UP -> mModel.scrollVertical(-20)
            ButtonActions.SCROLL_DOWN -> mModel.scrollVertical(20)
            ButtonActions.SCROLL_LEFT -> mModel.scrollHorizontal(20)
            ButtonActions.SCROLL_RIGHT -> mModel.scrollHorizontal(-20)
        }
    }

    /**
     * handler for mouse clicks on the canvas
     * calls the model to render a house on the position off the click
     */
    inner class MouseHandler : EventHandler<MouseEvent> {
        override fun handle(_event: MouseEvent) = mModel.generateHome(_event.x.toInt(), _event.y.toInt())
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