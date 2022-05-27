package at.shehata.ex3.client.gis.controller

import at.shehata.ex3.CANVAS_ID
import at.shehata.ex3.client.gis.model.GISModel
import at.shehata.ex3.client.gis.ui.GISView
import at.shehata.ex3.client.gis.ui.components.ButtonActions
import at.shehata.ex3.client.gis.ui.components.SCALE_FIELD_ID
import at.shehata.ex3.client.gis.ui.components.Server
import at.shehata.ex3.server.DummyGIS
import at.shehata.ex3.server.OSMServer
import at.shehata.ex3.server.VerwaltungsgrenzenServer
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.TextField
import javafx.scene.image.WritableImage
import javafx.scene.input.*
import javafx.stage.FileChooser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.PI


/**
 * contains the core logic that binds view and model
 *
 * @param mModel The model that contains the business logic
 */
class GISController(
	private val mModel: GISModel,
) {
	/**
	 * coroutine for the controller
	 */
	private val mScope = CoroutineScope(Dispatchers.Default)

	/// the singletons of the different handlers
	private val mActionHandler by lazy { ActionHandler() }
	private val mChangeHandler by lazy { ChangeHandler() }
	private val mMouseHandler by lazy { MouseHandler() }
	private val mKeyHandler by lazy { KeyHandler() }
	private val mScrollHandler by lazy { ScrollHandler() }

	/**
	 * The current view
	 */
	lateinit var mView: GISView

	/**
	 * Returns the Singleton of the ActionHandler
	 *
	 * @return The ActionHandler
	 * @see ActionHandler
	 */
	fun getActionHandler() = mActionHandler

	/**
	 * Returns the Singleton of the MouseHandler
	 *
	 * @return The MouseHandler
	 * @see MouseHandler
	 */
	fun getMouseHandler() = mMouseHandler

	/**
	 * Returns the Singleton of the ChangeHandler
	 *
	 * @return The ChangeHandler
	 * @see ChangeHandler
	 */
	fun getChangeHandler() = mChangeHandler

	/**
	 * Returns the Singleton of the KeyHandler
	 *
	 * @return The KeyHandler
	 * @see KeyHandler
	 */
	fun getKeyHandler() = mKeyHandler

	/**
	 * Returns the Singleton of the ScrollHandler
	 *
	 * @return The ScrollHandler
	 * @see ScrollHandler
	 */
	fun getScrollHandler() = mScrollHandler


	/**
	 * handler for the button
	 * calls the model to generate a house on a random position
	 */
	inner class ActionHandler : EventHandler<ActionEvent> {
		/**
		 * The current state of the POI visibility
		 */
		private var mIsPOIOn = false

		/**
		 * The current state of sticky
		 */
		private var mIsSticky = false

		/**
		 * checks the event source and calls the handler for it
		 */
		override fun handle(_event: ActionEvent) {
			when (_event.source) {
				is Button -> bottomBarHandler(_event)
				is RadioMenuItem -> menuBarHandler(_event)
			}
		}

		/**
		 * Checks the event source id and converts it to the enum
		 * value and sets the model server
		 *
		 * @param _event the ActionEvent that triggered it
		 */
		private fun menuBarHandler(_event: ActionEvent) {
			mModel.mServer = when (Server.valueOf((_event.source as RadioMenuItem).id)) {
				Server.OSM -> OSMServer()
				Server.DUMMY_GIS -> DummyGIS()
				Server.VERWALTUNGSGRENZEN -> VerwaltungsgrenzenServer()
			}
		}

		/**
		 * Handles the bottom bar button clicks
		 *
		 * @param _event the ActionEvent that triggered it
		 */
		private fun bottomBarHandler(_event: ActionEvent) {
			when (ButtonActions.valueOf((_event.source as Button).id)) {
				ButtonActions.SAVE -> saveToFile()
				ButtonActions.SCROLL_UP -> mScope.launch { mModel.scrollVerticalNonBlocking(20) }
				ButtonActions.SCROLL_DOWN -> mScope.launch { mModel.scrollVerticalNonBlocking(-20) }
				ButtonActions.SCROLL_LEFT -> mScope.launch { mModel.scrollHorizontalNonBlocking(20) }
				ButtonActions.SCROLL_RIGHT -> mScope.launch { mModel.scrollHorizontalNonBlocking(-20) }
				ButtonActions.ZOOM_TO_FIT -> mScope.launch { mModel.zoomToFitNonBlock() }
				ButtonActions.ZOOM_IN -> {
					mModel.zoom(1.3)
					mScope.launch { mModel.repaint() }
				}
				ButtonActions.ZOOM_OUT -> {
					mModel.zoom(1 / 1.3)
					mScope.launch { mModel.repaint() }
				}
				ButtonActions.DRAW -> {
					mScope.launch {
						mModel.loadData()
						mModel.zoomToFit()
						mModel.repaint()
					}
				}
				ButtonActions.TOGGLE_POI -> {
					when (mIsPOIOn) {
						true -> {
							(_event.source as Button).text = "Show POI"
							mScope.launch {
								mModel.hidePOI()
							}
						}
						false -> {
							(_event.source as Button).text = "Hide POI"
							mScope.launch {
								mModel.loadPOIData()
								mModel.repaint()
							}
						}
					}
					mIsPOIOn = !mIsPOIOn
				}
				ButtonActions.STICKY -> {
					val btn = (_event.source as Button)
					when (mIsSticky) {
						true -> {
							btn.text = "Sticky"
							mScope.launch {
								mModel.loadData()
								mModel.repaint()
							}
						}
						false -> {
							btn.text = "Disable Sticky"
							mScope.launch {
								mModel.loadAreaData()
								mModel.repaint()
							}

						}
					}
					mIsSticky = !mIsSticky
				}
			}
		}

		/**
		 * Saves the Canvas as a png file using the FileChooser
		 *
		 * @see javafx.stage.FileChooser
		 */
		private fun saveToFile() {
			FileChooser().apply {
				title = "Save"
				extensionFilters += FileChooser.ExtensionFilter("PNG", "*.png")
				val file = showSaveDialog(mView.scene.window)?.apply {
					if (!name.endsWith(".png")) renameTo(File("$absolutePath.png"))
				} ?: return@apply
				try {
					val canvas = mView.lookup("#${CANVAS_ID}") as Canvas
					val writableImage = WritableImage(canvas.width.toInt(), canvas.height.toInt())
					canvas.snapshot(null, writableImage)
					ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file)
				} catch (_e: IOException) {
					_e.printStackTrace()
				}
			}
		}
	}

	/**
	 * handler for mouse clicks on the canvas
	 * calls the model to render a house on the position off the click
	 */
	inner class MouseHandler : EventHandler<MouseEvent> {
		/**
		 * stores the difference between each drag
		 */
		private val mDeltaDrag = Point()

		/**
		 * stores the position of the pressed Event
		 */
		private val mStartPoint = Point()

		/**
		 * stores the size of the overlay rect
		 */
		private val mOverlayRect = Rectangle()

		/**
		 * owner for the clipboard
		 */
		private val mOwner = StringSelection("GIS")

		/**
		 * handles press/drag/release events of the mouse
		 */
		override fun handle(_event: MouseEvent) {
			when (_event.eventType) {
				MouseEvent.MOUSE_PRESSED -> mousePressedHandler(_event)
				MouseEvent.MOUSE_DRAGGED -> mouseDraggedHandler(_event)
				MouseEvent.MOUSE_RELEASED -> mouseReleaseHandler(_event)
			}
		}


		/**
		 * Sets the properties and checks which mouse button
		 * has been pressed to either save the canvas
		 * or to save to the clipboard
		 *
		 * @param _event the event of handle
		 */
		private fun mousePressedHandler(_event: MouseEvent) {
			val x = _event.x.toInt()
			val y = _event.y.toInt()

			mDeltaDrag.setLocation(x, y)
			mStartPoint.setLocation(x, y)
			mOverlayRect.setRect(x.toDouble(), y.toDouble(), 1.0, 1.0)

			when (_event.button) {
				MouseButton.SECONDARY -> mView.saveContext()
				MouseButton.PRIMARY -> saveToClipboard(_event)
				else -> return
			}
		}

		/**
		 * Translates the canvas or draws the overlay rect
		 * depending on the button
		 *
		 * @param _event the event of handle
		 */
		private fun mouseDraggedHandler(_event: MouseEvent) {
			mView.cursor = when (_event.button) {
				MouseButton.PRIMARY -> {
					val deltaX = _event.x.toInt() - mStartPoint.x
					val deltaY = _event.y.toInt() - mStartPoint.y
					when {
						deltaX == 0 && deltaY == 0 -> Cursor.DEFAULT
						else -> {
							setOverlay(_event, deltaX, deltaY)
							mView.drawXOR(mOverlayRect)

							Cursor.CROSSHAIR
						}
					}
				}
				MouseButton.SECONDARY -> {
					val dx = _event.x - mDeltaDrag.x.toDouble()
					val dy = _event.y - mDeltaDrag.y.toDouble()
					mDeltaDrag.setLocation(_event.x, _event.y)
					mView.translate(dx, dy)

					Cursor.MOVE
				}
				else -> Cursor.DEFAULT
			}
		}

		/**
		 * Finishes the translation of the canvas or
		 * zooms and clears the overlay depending on
		 * the hold button on release
		 *
		 * @param _event the event of handle
		 */
		private fun mouseReleaseHandler(_event: MouseEvent) {
			val deltaX = _event.x.toInt() - mStartPoint.x
			val deltaY = _event.y.toInt() - mStartPoint.y
			when (_event.button) {
				MouseButton.PRIMARY -> {
					if (deltaX == 0 && deltaY == 0) return
					mView.clearXOR()
					mScope.launch { mModel.zoomRectNonBlock(mOverlayRect) }
				}
				MouseButton.SECONDARY -> {
					mModel.scrollHorizontal(deltaX)
					mModel.scrollVertical(deltaY)
					mView.restoreContext()
					mScope.launch { mModel.repaint() }
				}
				else -> return
			}
			mView.cursor = Cursor.DEFAULT
		}

		/**
		 * Calculates the size of the overlay rect and sets it to that
		 *
		 * @param _event the event of handle
		 * @param _deltaX the difference between the current x position and start position
		 * @param _deltaY the difference between the current y position and start position
		 */
		private fun setOverlay(_event: MouseEvent, _deltaX: Int, _deltaY: Int) {
			val (x, width) = when {
				_deltaX < 0 -> _event.x to mStartPoint.x - _event.x
				_deltaX > 0 -> mStartPoint.x.toDouble() to _deltaX.toDouble()
				else -> return
			}

			val (y, height) = when {
				_deltaY < 0 -> _event.y to mStartPoint.y - _event.y
				_deltaY > 0 -> mStartPoint.y.toDouble() to _deltaY.toDouble()
				else -> return
			}

			mOverlayRect.setRect(x, y, width, height)
		}

		/**
		 * Saves the current mouse position in the clipboard
		 *
		 * @param _event the event of handle
		 */
		private fun saveToClipboard(_event: MouseEvent) {
			Toolkit.getDefaultToolkit().systemClipboard.apply {
				val data = mModel.getMapPoint(Point(_event.x.toInt(), _event.y.toInt())).let { "(${it.x},${it.y})\n" }
				if (_event.isControlDown) {
					val oldData = getContents(this@MouseHandler).getTransferData(DataFlavor.stringFlavor) as String
					setContents(StringSelection(oldData + data), mOwner)
					return
				}
				setContents(StringSelection(data), mOwner)
			}
		}
	}

	/**
	 * Handler for the Keyboard Keys
	 * It translates and rotates when the correct Key is clicked
	 */
	inner class KeyHandler : EventHandler<KeyEvent> {

		/**
		 * handles the key released/pressed events
		 */
		override fun handle(_event: KeyEvent) {
			when (_event.eventType) {
				KeyEvent.KEY_RELEASED -> handleKeyReleased(_event)
				KeyEvent.KEY_PRESSED -> handleKeyPress(_event.code)
			}
		}

		/**
		 * helper function for Key_Released
		 * Handles Rotating, when R is clicked
		 * Handles Zooming, when ENTER is clicked
		 * inside the Scale-TextField
		 *
		 * @param _event the event of handle
		 */
		private fun handleKeyReleased(_event: KeyEvent) {
			val source = _event.source
			when (_event.code) {
				KeyCode.R -> if (_event.isShiftDown) mModel.rotate(-PI / 2) else mModel.rotate(PI / 2)
				KeyCode.ENTER -> when (source) {
					is TextField -> when (source.id) {
						SCALE_FIELD_ID -> mModel.zoomToScale(source.text.toInt())
						else -> return
					}
					else -> return
				}
				else -> return
			}
			mScope.launch { mModel.repaint() }
		}

		/**
		 * helper function for Key_Press
		 * Handles Scrolling, when A, S, D or W is clicked
		 *
		 * @param _eventCode The code of the clicked Button
		 */
		private fun handleKeyPress(_eventCode: KeyCode) {
			when (_eventCode) {
				KeyCode.W -> mModel.scrollVertical(20)
				KeyCode.S -> mModel.scrollVertical(-20)
				KeyCode.A -> mModel.scrollHorizontal(20)
				KeyCode.D -> mModel.scrollHorizontal(-20)
				else -> return
			}
			mScope.launch { mModel.repaint() }
		}
	}

	inner class ScrollHandler : EventHandler<ScrollEvent> {

		/**
		 * Zooms in and out on the current mouse position
		 * depending on the mousewheel direction
		 */
		override fun handle(_event: ScrollEvent) {
			val pt = Point(_event.x.toInt(), _event.y.toInt())
			val factor = if (_event.deltaY > 0) 1.1 else 1 / 1.1
			mModel.zoom(pt, factor)
			mScope.launch { mModel.repaint() }
		}
	}

	/**
	 * handler that listens for resizes of the canvas
	 */
	inner class ChangeHandler : ChangeListener<Number> {

		/**
		 * Updates the size of the drawing image
		 */
		override fun changed(_observable: ObservableValue<out Number>, _oldValue: Number, _newValue: Number) {
			when ((_observable as ReadOnlyDoubleProperty).name) {
				"width" -> mModel.setWidth(_newValue.toInt())
				"height" -> mModel.setHeight(_newValue.toInt())
			}
			mScope.launch { mModel.repaint() }
		}
	}
}