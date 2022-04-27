package at.shehata.ex1

import at.shehata.ex1.gis.GISController
import at.shehata.ex1.gis.GISModel
import at.shehata.ex1.gis.GISView
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage


class GISApplication : Application() {
    companion object {
        @JvmStatic
        fun main(_args: Array<String>) {
            launch(GISApplication::class.java)
        }

        const val CANVAS_ID = "my-canvas"
        const val SCENE_HEIGHT = 480.0
        const val SCENE_WIDTH = 640.0
    }

    private lateinit var mRoot: GISView
    private lateinit var mController: GISController

    override fun init() {
        val model = GISModel()
        mController = GISController(model)
        mRoot = GISView(mController)
        model.addMapObserver(mRoot)
    }

    override fun start(_stage: Stage) {
        val main = Scene(mRoot, SCENE_WIDTH, SCENE_HEIGHT)

        _stage.title = "GIS"
        _stage.scene = main
        _stage.show()
    }
}

