package at.shehata.ex1.gis

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

class GISController(private val mModel: GISModel) {
    private val mActionHandler by lazy { ActionHandler() }
    private val mChangeHandler by lazy { ChangeHandler() }
    private val mMouseHandler by lazy { MouseHandler() }

    fun getActionHandler() = mActionHandler
    fun getMouseHandler() = mMouseHandler
    fun getChangeHandler() = mChangeHandler

    inner class ActionHandler : EventHandler<ActionEvent> {
        override fun handle(_event: ActionEvent) = mModel.generateRndHome()
    }

    inner class MouseHandler : EventHandler<MouseEvent> {
        override fun handle(_event: MouseEvent) = mModel.generateHome(_event.x.toInt(), _event.y.toInt())
    }

    inner class ChangeHandler : ChangeListener<Number> {
        override fun changed(_observable: ObservableValue<out Number>, _oldValue: Number, _newValue: Number) {
            when ((_observable as ReadOnlyDoubleProperty).name) {
                "width" -> mModel.setWidth(_newValue.toInt())
                "height" -> mModel.setHeight(_newValue.toInt())
            }
        }
    }

}