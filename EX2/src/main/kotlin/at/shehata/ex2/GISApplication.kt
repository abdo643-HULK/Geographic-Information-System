package at.shehata.ex2

import at.shehata.ex2.gis.GISController
import at.shehata.ex2.gis.GISModel
import at.shehata.ex2.gis.GISView
import at.shehata.ex2.utils.Matrix
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
        fun main(_args: Array<String>) {
            launch(GISApplication::class.java)

            val m1 = Matrix(
                doubleArrayOf(
                    2.0, 9.0, 1.0,
                    2.0, 7.0, 2.0,
                    5.0, 0.0, 1.0
                )
            )

            val m2 = Matrix(
                doubleArrayOf(
                    1.0, 0.0, 7.0,
                    0.0, 2.0, 0.0,
                    9.0, 8.0, 5.0
                )
            )

            println(m1 * m2)

            println(
                Matrix(
                    doubleArrayOf(
                        3.0, 0.0, -2.0,
                        -3.0, 0.0, -2.0,
                        3.0, 3.0, 1.0,
                    )
                )
                    .inverse()
            )

            println(m1 * Point(2, 9))
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

