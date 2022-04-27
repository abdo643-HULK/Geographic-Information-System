package at.shehata.ex1.gis

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
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
        override fun handle(_event: ActionEvent) = mModel.generateRndHome()
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