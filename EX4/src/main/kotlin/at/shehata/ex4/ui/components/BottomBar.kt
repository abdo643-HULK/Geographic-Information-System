package at.shehata.ex4.ui.components

import at.shehata.ex4.ui.GISView
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
class BottomBar : HBox() {
	init {
		start()
	}

	/**
	 * Creates the buttons and adds them to the BottomBar
	 */
	private fun start() {
		style += "-fx-background-color: white; -fx-padding: 7 12"
		spacing = 7.0
		alignment = Pos.BOTTOM_LEFT

		val drawBtn = Button("Draw").apply {
			id = ButtonActions.DRAW.name
		}

		val zoomToFitBtn = Button("ZTF").apply {
			id = ButtonActions.ZOOM_TO_FIT.name
		}

		val zoomOutBtn = Button("-").apply {
			id = ButtonActions.ZOOM_OUT.name
		}

		val zoomInBtn = Button("+").apply {
			id = ButtonActions.ZOOM_IN.name
		}

		val moveBtns = GridPane().apply {
			hgap = 5.0
			vgap = 5.0
			style += "-fx-padding: 0 10;"

			val scrollLeftBtn = Button("L").apply {
				id = ButtonActions.SCROLL_LEFT.name
				add(this, 0, 1)
			}

			val scrollUpBtn = Button("U").apply {
				id = ButtonActions.SCROLL_UP.name
				add(this, 1, 0)
			}

			val scrollDownBtn = Button("D").apply {
				id = ButtonActions.SCROLL_DOWN.name
				add(this, 1, 1)
			}

			val scrollRightBtn = Button("R").apply {
				id = ButtonActions.SCROLL_RIGHT.name
				add(this, 2, 1)
			}
		}

		val scaleLabel = Label("1:").apply {
			padding = Insets(4.5, 0.0, 4.5, 0.0)
		}

		val scaleField = TextField("unknown").apply {
			id = GISView.SCALE_FIELD_ID
			padding = Insets(4.5, 0.0, 4.5, 2.0)
		}

		val poiBtn = Button("Show POI").apply {
			id = ButtonActions.TOGGLE_POI.name
		}

		val stickyBtn = Button("Sticky").apply {
			id = ButtonActions.STICKY.name
		}

		val storeBtn = Button("Store").apply {
			id = ButtonActions.SAVE.name
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