package at.shehata.ex4.ui.components

import at.shehata.ex4.GISApplication
import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeType
import javafx.scene.text.Font
import javafx.scene.text.Text
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeView : HBox(), PositionUpdateListener {

    private val mTime by lazy {
        Text().apply {
            fill = Color.WHITE
            font = Font(GISApplication.DEFAULT_FONT_FAMILY, 32.0)
            stroke = Color.BLACK
            strokeWidth = 2.0
            strokeType = StrokeType.OUTSIDE
            cacheHint = CacheHint.QUALITY
        }
    }

    init {
        background = Background.fill(Color.web("#BDB76B"))
        padding = Insets(5.0)
        alignment = Pos.CENTER

        val time = LocalDateTime
            .now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        mTime.text = time
        children += mTime
    }

    override fun update(_info: NMEAInfo) {
        val hour = _info.mTime?.slice(0, 2)
        val minute = _info.mTime?.slice(2, 2)
        val seconds = _info.mTime?.slice(4, 2)
        mTime.text = "$hour:$minute:$seconds"
    }
}