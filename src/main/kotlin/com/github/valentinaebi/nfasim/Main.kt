package com.github.valentinaebi.nfasim

import com.github.valentinaebi.nfasim.gui.ControlledAutomatonPane
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {

    override fun start(stage: Stage) {
        stage.minWidth = 1415.0
        stage.minHeight = 600.0
        stage.isMaximized = true
        stage.scene = Scene(ControlledAutomatonPane(stage))
        stage.show()
    }

}

fun main() {
    Application.launch(Main::class.java)
}