package at.shehata.ex1.gis

import javafx.scene.control.*
import javafx.scene.control.MenuBar


class MenuBar : MenuBar() {
    companion object {
        const val MENU_B_ITEM_01 = "SERVER_A"
        const val MENU_B_ITEM_02 = "SERVER_B"
    }

    init {
        menus.addAll(createA(), createB())
    }

    private fun createA(): Menu {
        val menu = Menu("A")
        val menuItem01 = MenuItem("Test")
        menuItem01.setOnAction { println("Menu Item Test pressed") }
        menu.items.addAll(menuItem01)
        return menu
    }

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