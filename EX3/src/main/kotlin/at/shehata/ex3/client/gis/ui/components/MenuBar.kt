package at.shehata.ex3.client.gis.ui.components

import at.shehata.ex3.client.gis.controller.GISController
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ToggleGroup

/**
 * Enum of the available Servers
 */
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
	/**
	 * creates the Menus and adds them to the MenuBar
	 */
	init {
		menus += arrayOf(createA(), createB(_actionHandler))
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
	 * Creates the Menu 'Server' with all available
	 * Servers in a ToggleGroup
	 *
	 * @return The Menu 'Server'
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