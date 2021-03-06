package at.shehata.ex2.gis.components

import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ToggleGroup

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
    private fun createA(): Menu {
        val menu = Menu("A")
        val menuItem01 = MenuItem("Test")
        menuItem01.setOnAction { println("Menu Item Test pressed") }
        menu.items.addAll(menuItem01)
        return menu
    }

    /**
     * Creates the Menu 'B' with two item
     * in a ToggleGroup
     *
     * @return The Menu 'B'
     * @see javafx.scene.control.Menu
     */
    private fun createB(): Menu {
        val menu = Menu("B")
        val serverA = RadioMenuItem("Server-A")
        serverA.id = MENU_B_ITEM_01
        serverA.setOnAction { println("Menu Item Server-A pressed") }
        val serverB = RadioMenuItem("Server-B")
        serverB.id = MENU_B_ITEM_02
        serverB.setOnAction { println("Menu Item Server-B pressed") }

        val tg = ToggleGroup()
        serverA.toggleGroup = tg
        serverA.isSelected = true
        serverB.toggleGroup = tg

        menu.items.addAll(serverA, serverB)

        return menu
    }
}