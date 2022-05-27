package at.shehata.ex3.client.gis.ui.components

import at.shehata.ex3.client.gis.controller.GISController
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

/**
 * ID of the TextField that controls the current scale
 */
const val SCALE_FIELD_ID = "scale-field"

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
	TOGGLE_POI,
	SAVE,
	STICKY
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
		style += "-fx-background-color: white; -fx-padding: 7 12"
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

			Button("L").apply {
				id = ButtonActions.SCROLL_LEFT.name
				onAction = _actionHandler
				add(this, 0, 1)
			}

			Button("U").apply {
				id = ButtonActions.SCROLL_UP.name
				onAction = _actionHandler
				add(this, 1, 0)
			}

			Button("D").apply {
				id = ButtonActions.SCROLL_DOWN.name
				onAction = _actionHandler
				add(this, 1, 1)
			}

			Button("R").apply {
				id = ButtonActions.SCROLL_RIGHT.name
				onAction = _actionHandler
				add(this, 2, 1)
			}
		}

		val scaleLabel = Label("1:").apply {
			padding = Insets(4.5, 0.0, 4.5, 0.0)
		}

		val scaleField = TextField("unknown").apply {
			id = SCALE_FIELD_ID
			padding = Insets(4.5, 0.0, 4.5, 2.0)
			onKeyReleased = _keyHandler
		}

		val poiBtn = Button("Show POI").apply {
			id = ButtonActions.TOGGLE_POI.name
			onAction = _actionHandler
		}

		val stickyBtn = Button("Sticky").apply {
			id = ButtonActions.STICKY.name
			onAction = _actionHandler
		}

		val storeBtn = Button("Store").apply {
			id = ButtonActions.SAVE.name
			onAction = _actionHandler
		}

		children += arrayOf(
			drawBtn,
			zoomToFitBtn,
			Region().apply {
				setHgrow(this, Priority.ALWAYS)
			},
			zoomOutBtn,
			zoomInBtn,
			moveBtns,
			HBox().apply {
				alignment = Pos.BOTTOM_LEFT
				spacing = 2.0
				children += arrayOf(scaleLabel, scaleField)
			},
			poiBtn,
			stickyBtn,
			Region().apply {
				setHgrow(this, Priority.ALWAYS)
			},
			storeBtn
		)
	}
}