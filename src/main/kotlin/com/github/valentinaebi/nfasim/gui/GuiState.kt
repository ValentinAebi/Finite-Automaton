package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Font
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State as State

class GuiState(val underlyingState: State): Group() {
    private val circle = Circle(radius, colorInactive)
    private val shadow = Circle(shadowRadius, colorShadow)
    private val nameLabel = Label(underlyingState.id)

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

    init {
        nameLabel.font = font
        shadow.isVisible = false
        children.addAll(shadow, circle, nameLabel)
        setOnMouseClicked { event -> if (event.isStillSincePress) isSelected = !isSelected }
        setOnMouseDragged { event -> layoutX = event.sceneX ; layoutY = event.sceneY }
        updateLayout()
    }

    fun updateLayout(){
        nameLabel.layoutXProperty().bind(circle.layoutXProperty().subtract(nameLabel.boundsInParent.width / 2.0))
        nameLabel.layoutYProperty().bind(circle.layoutYProperty().subtract(12.5))
    }

    companion object {
        const val radius = 35.0
        private const val shadowRadius = 45.0
        private val font = Font("cambria", 24.0)
        private val colorInactive = Color.YELLOW
        private val colorActive = Color.ORANGE
        private val colorShadow = Color.LIGHTGRAY
    }

}