package com.github.valentinaebi.nfasim

import com.github.valentinaebi.nfasim.gui.ControlledAutomatonPane
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {

    override fun start(stage: Stage) {
        stage.width = 1000.0
        stage.height = 700.0
        stage.scene = Scene(ControlledAutomatonPane())
        stage.show()
    }

}

fun main() {
    Application.launch(Main::class.java)
}