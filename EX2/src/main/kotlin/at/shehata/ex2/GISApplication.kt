package at.shehata.ex2

import at.shehata.ex2.gis.GISController
import at.shehata.ex2.gis.GISModel
import at.shehata.ex2.gis.GISView
import at.shehata.ex2.utils.Matrix
import at.shehata.ex2.utils.testZTF
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.awt.Point

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
//        fun main(_args: Array<String>) = launch(GISApplication::class.java)
        fun main(_args:Array<String>) {
            launch(GISApplication::class.java)
            testZTF()
        }

        /**
         * The ID of the Canvas for lookup
         */
        const val CANVAS_ID = "my-canvas"

        /**
         * Initial size of the Scene
         */
        const val SCENE_HEIGHT = 480.0
        const val SCENE_WIDTH = 640.0
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
        val controller = GISController(model)
        mRoot = GISView(controller)
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

