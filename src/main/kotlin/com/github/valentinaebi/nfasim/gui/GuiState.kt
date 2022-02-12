package com.github.valentinaebi.nfasim.gui

import javafx.beans.binding.Bindings
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Polygon
import javafx.scene.text.Font
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State as State

class GuiState(val underlyingState: State): Group() {
    private val circle = Circle(radius, colorInactive)
    private val shadow = Circle(shadowRadius, colorShadow)
    private val nameLabel = Label(underlyingState.id)
    private val startTriangle = Polygon()

    var isSelected = false
        set(_isSelected){
            field = _isSelected
            shadow.isVisible = isSelected
        }
    var isActive = false
        set(_isActive){
            field = _isActive
            circle.fill = if (isActive) colorActive else colorInactive
        }
    var isInit = false
        set(_isInit){
            field = _isInit
            startTriangle.isVisible = isInit
        }
    var isAccepting = false
        set(_isAccepting){
            field = _isAccepting
            TODO("mark a state as accepting or not")
        }

    init {
        nameLabel.font = font
        shadow.isVisible = false
        startTriangle.points.setAll(listOf(-30.0 - radius, 10.0, -30.0 - radius, -10.0, -radius, 0.0))
        startTriangle.fill = colorTriangle
        startTriangle.isVisible = false
        children.addAll(shadow, circle, nameLabel, startTriangle)
        nameLabel.layoutX = -nameLabel.boundsInParent.width / 2.0
        nameLabel.layoutY = -12.5
        setOnMouseClicked { event -> if (event.isStillSincePress) isSelected = !isSelected }
        setOnMouseDragged { event -> layoutX = event.sceneX ; layoutY = event.sceneY }
    }

    companion object {
        const val radius = 35.0
        private const val shadowRadius = 45.0
        private const val triangleLength = 25.0
        private const val triangleHeight = 10.0
        private val font = Font("cambria", 24.0)
        private val colorInactive = Color.YELLOW
        private val colorActive = Color.ORANGE
        private val colorShadow = Color.LIGHTGRAY
        private val colorTriangle = Color.GREEN
    }

}