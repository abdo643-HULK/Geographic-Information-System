package at.shehata.ex3.client.gis.components

import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ToggleGroup

enum class ServerMenuActions {
    
}

/**
 * MenuBar component to separate view into smaller
 * pieces.
 * Creates a MenuBar with 2 Menus
 */
class MenuBar : javafx.scene.control.MenuBar() {
    companion object {
        /**
         * Id of the first item in Menu 'B'
         */
        const val MENU_B_ITEM_01 = "SERVER_A"

        /**
         * Id of the second item in Menu 'B'
         */
        const val MENU_B_ITEM_02 = "SERVER_B"
    }

    init {
        menus.addAll(createA(), createB())
    }

    /**
     * Creates the Menu 'A' with one menu item
     *
     * @return The Menu 'A'
     * @see javafx.scene.control.Menu
     */
    private fun createA() = Menu("A").apply {
        val menuItem01 = MenuItem("Test").apply {
            setOnAction { println("Menu Item Test pressed") }
        }
        items.addAll(menuItem01)
    }

    /**
     * Creates the Menu 'B' with two item
     * in a ToggleGroup
     *
     * @return The Menu 'B'
     * @see javafx.scene.control.Menu
     */
    private fun createB() = Menu("Server").apply {
        val tg = ToggleGroup()
        val serverA = RadioMenuItem("3857").apply {
            id = MENU_B_ITEM_01
            toggleGroup = tg
            isSelected = true
            setOnAction { println("Menu Item Server-A pressed") }
        }
        val serverB = RadioMenuItem("4326").apply {
            id = MENU_B_ITEM_02
            toggleGroup = tg
            setOnAction { println("Menu Item Server-B pressed") }
        }

        items.addAll(serverA, serverB)
    }
}