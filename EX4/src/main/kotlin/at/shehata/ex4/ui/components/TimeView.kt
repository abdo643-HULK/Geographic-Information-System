package at.shehata.ex4.ui.components

import at.shehata.ex4.interfaces.PositionUpdateListener
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.HBox
import org.intellij.lang.annotations.Language
import java.awt.Image
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeView : HBox(), PositionUpdateListener {

    init {
        // language=CSS prefix=javafx_dummy_selector{ suffix=}
        style = "-fx-background-color: #BDB76B; -fx-font-size: 22;"
        padding = Insets(5.0)
        alignment = Pos.CENTER

        val time = Label()
        time.text = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        children += time
    }

    override fun update(_img: Image, _scale: Int) {
        TODO("Not yet implemented")
    }
}