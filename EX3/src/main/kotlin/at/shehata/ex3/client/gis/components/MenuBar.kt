package at.shehata.ex3.client.gis.components

import at.shehata.ex3.client.gis.GISController
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ToggleGroup

enum class Server(val mValue: String) {
	OSM("OSM-Hagenberg"),
	DUMMY_GIS("Dummy-GIS"),
	VERWALTUNGSGRENZEN("Verwaltungsgrenzen")
}

/**
 * MenuBar component to separate view into smaller
 * pieces.
 * Creates a MenuBar with 2 Menus
 */
class MenuBar(
	_actionHandler: GISController.ActionHandler
) : javafx.scene.control.MenuBar() {
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
		menus.addAll(createA(), createB(_actionHandler))
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
	private fun createB(_actionHandler: GISController.ActionHandler) = Menu("Server").apply {
		val tg = ToggleGroup()

		val menuItems = Server.values().map {
			RadioMenuItem(it.mValue).apply {
				id = it.name
				toggleGroup = tg
				onAction = _actionHandler
			}
		}

		menuItems[0].isSelected = true
		items += menuItems
	}
}