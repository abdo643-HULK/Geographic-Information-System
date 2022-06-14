package at.shehata.ex4

import at.shehata.ex4.nmea.NMEAParser
import at.shehata.ex4.ui.views.GNSSView
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.text.Font
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
		const val SCENE_HEIGHT = 520.0

		//		const val SCENE_WIDTH = 720.0
		const val SCENE_WIDTH = 860.0

		const val DEFAULT_FONT_FAMILY = "Iosevka Nerd Font"
	}

	/**
	 * Holds our View for init
	 */
	private lateinit var mRoot: GNSSView

	private inline fun loadFonts() {
		arrayOf("Iosevka_Nerd_Font_Complete", "Iosevka_Bold_Nerd_Font_Complete").forEach {
			javaClass
				.getResource("/fonts/$it.ttf")
				?.apply { Font.loadFont(toExternalForm(), 10.0) }
		}
	}

	/**
	 * initializes the MVC Objects
	 */
	override fun init() {
		loadFonts()

		mRoot = GNSSView(
			NMEAParser().apply {
				parse()
			}
		)
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

