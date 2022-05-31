package at.shehata.ex4

import at.shehata.ex4.model.GISModel
import at.shehata.ex4.ui.GISView
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * The single instance of the application that runs
 * the Scene and initializes all the Objects for the MVC pattern
 */
class GISApplication : Application() {
	companion object {
		/**
		 * starts the Application
		 * must be JvmStatic
		 */
		@JvmStatic
		fun main(_args: Array<String>) = launch(GISApplication::class.java)

		/**
		 * The ID of the Canvas for lookup
		 */
		const val CANVAS_ID = "my-canvas"
		const val OVERLAY_ID = "my-canvas-overlay"

		/**
		 * Initial size of the Scene
		 */
		const val SCENE_HEIGHT = 480.0 * 1.205
		const val SCENE_WIDTH = 740.0
	}

	/**
	 * Holds our View for init
	 */
	private lateinit var mRoot: GISView

	/**
	 * initializes the MVC Objects
	 */
	override fun init() {
		val model = GISModel()
		mRoot = GISView()
		model.addMapObserver(mRoot)
	}

	/**
	 * sets the scene and title and shows it
	 */
	override fun start(_stage: Stage) {
		val main = Scene(mRoot, SCENE_WIDTH, SCENE_HEIGHT)

		_stage.title = "GIS"
		_stage.scene = main
		_stage.show()
	}
}

