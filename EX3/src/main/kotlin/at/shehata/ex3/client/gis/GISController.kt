package at.shehata.ex3.client.gis

import at.shehata.ex3.GISApplication
import at.shehata.ex3.client.gis.components.ButtonActions
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
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

    fun getScrollHandler() = mScrollHandler


    /**
     * handler for the button
     * calls the model to generate a house on a random position
     */
    inner class ActionHandler : EventHandler<ActionEvent> {
        private var mIsPOIOn = false
        private var mIsSticky = false

        @Suppress("MagicNumber")
        override fun handle(_event: ActionEvent): Unit = when (ButtonActions.valueOf((_event.source as Button).id)) {
            ButtonActions.SAVE -> saveToFile()
            ButtonActions.ZOOM_IN -> mModel.zoom(1.3)
            ButtonActions.ZOOM_OUT -> mModel.zoom(1 / 1.3)
            ButtonActions.SCROLL_UP -> mModel.scrollVertical(20)
            ButtonActions.SCROLL_DOWN -> mModel.scrollVertical(-20)
            ButtonActions.SCROLL_LEFT -> mModel.scrollHorizontal(20)
            ButtonActions.SCROLL_RIGHT -> mModel.scrollHorizontal(-20)
            ButtonActions.ZOOM_TO_FIT -> {
                mScope.launch { mModel.zoomToFitNonBlock() }
                Unit
            }
            ButtonActions.DRAW -> {
                mScope.launch {
                    mModel.loadData()
                    mModel.repaint()
                }
                Unit
            }
            ButtonActions.TOGGLE_POI -> {
                when (mIsPOIOn) {
                    true -> {
                        (_event.source as Button).text = "Show POI"
                        mModel.hidePOI()
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

        private fun saveToFile() {
            FileChooser().apply {
                title = "Save"
                extensionFilters += FileChooser.ExtensionFilter("PNG", "*.png")
                var file = showSaveDialog(mView.scene.window) ?: return@apply
                file = if (!file.endsWith(".png")) File(file.absolutePath + ".png") else file
                try {
                    val canvas = mView.lookup("#${GISApplication.CANVAS_ID}") as Canvas
                    val writableImage = WritableImage(canvas.width.toInt(), canvas.height.toInt())
                    canvas.snapshot(null, writableImage)
                    val renderedImage = SwingFXUtils.fromFXImage(writableImage, null)
                    ImageIO.write(renderedImage, "png", file)
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
        private val mDeltaDrag = Point()
        private val mStartPoint = Point()
        private val mOverlayRect = Rectangle()

        private val mOwner = StringSelection("GIS")

        override fun handle(_event: MouseEvent) {
            when (_event.eventType) {
                MouseEvent.MOUSE_PRESSED -> mousePressedHandler(_event)
                MouseEvent.MOUSE_DRAGGED -> mouseDraggedHandler(_event)
                MouseEvent.MOUSE_RELEASED -> mouseReleaseHandler(_event)
            }
        }


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

        private fun mouseReleaseHandler(_event: MouseEvent) {
            val deltaX = _event.x.toInt() - mStartPoint.x
            val deltaY = _event.y.toInt() - mStartPoint.y

            when (_event.button) {
                MouseButton.PRIMARY -> {
                    if (deltaX == 0 || deltaY == 0) return
                    mView.clearXOR()
                    mScope.launch { mModel.zoomRectNonBlock(Rectangle(mStartPoint.x, mStartPoint.y, deltaX, deltaY)) }
                }
                MouseButton.SECONDARY -> {
                    mModel.scrollHorizontal(deltaX)
                    mModel.scrollVertical(deltaY)
                    mView.restoreContext()
                    mModel.repaint()
                }
                else -> Unit
            }
            mView.cursor = Cursor.DEFAULT
        }

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
        override fun handle(_event: KeyEvent) {
            when (_event.eventType) {
                KeyEvent.KEY_RELEASED -> handleKeyReleased(_event)
                KeyEvent.KEY_PRESSED -> handleKeyPress(_event.code)
            }
        }

        /**
         * helper function for Key_Released
         * Handles Scrolling, when LEFT, DOWN, RIGHT or UP is clicked
         * Handles Rotating, when R is clicked
         */
        private fun handleKeyReleased(_event: KeyEvent) {
            when (_event.code) {
                KeyCode.R -> if (_event.isShiftDown) mModel.rotate(-PI / 2) else mModel.rotate(PI / 2)
                KeyCode.ENTER -> when (_event.source) {
                    is TextField -> mModel.zoomToScale((_event.source as TextField).text.toInt())
                    else -> return
                }
                else -> return
            }
            mModel.repaint()
        }

        /**
         * helper function for Key_Press
         * Handles Scrolling, when A, S, D or W is clicked
         */
        private fun handleKeyPress(_eventCode: KeyCode) {
            when (_eventCode) {
                KeyCode.W -> mModel.scrollVertical(20)
                KeyCode.S -> mModel.scrollVertical(-20)
                KeyCode.A -> mModel.scrollHorizontal(20)
                KeyCode.D -> mModel.scrollHorizontal(-20)
                else -> return
            }
            mModel.repaint()
        }
    }

    inner class ScrollHandler : EventHandler<ScrollEvent> {
        override fun handle(_event: ScrollEvent) {
            val pt = Point(_event.x.toInt(), _event.y.toInt())
            val factor = if (_event.deltaY > 0) 1.2 else 1 / 1.2
            mModel.zoom(pt, factor)
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