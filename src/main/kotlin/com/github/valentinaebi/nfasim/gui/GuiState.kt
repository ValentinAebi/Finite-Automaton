package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Polygon
import javafx.scene.shape.StrokeType
import javafx.scene.text.Font

class GuiState(val underlyingState: State): Group() {
    private val mainCircle = Circle(radius, colorInactive)
    private val acceptMarkerCircle = Circle(radius + strokeWidth, Color.TRANSPARENT)
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
            mainCircle.fill = if (isActive) colorActive else colorInactive
        }
    var isInit = false
        set(_isInit){
            field = _isInit
            startTriangle.isVisible = isInit
        }
    var isAccepting = false
        set(_isAccepting){
            field = _isAccepting
            acceptMarkerCircle.isVisible = isAccepting
        }

    init {
        mainCircle.strokeWidth = strokeWidth
        mainCircle.stroke = colorStroke
        mainCircle.strokeType = StrokeType.INSIDE
        acceptMarkerCircle.strokeWidth = strokeWidth
        acceptMarkerCircle.stroke = colorStroke
        acceptMarkerCircle.strokeType = StrokeType.OUTSIDE
        acceptMarkerCircle.isVisible = false
        nameLabel.font = font
        shadow.isVisible = false
        startTriangle.points.setAll(listOf(
            -triangleLength - radius, triangleHeight / 2.0,
            -triangleLength - radius, -triangleHeight / 2.0,
            -radius, 0.0
        ))
        startTriangle.fill = colorTriangle
        startTriangle.isVisible = false
        children.addAll(shadow, acceptMarkerCircle, mainCircle, nameLabel, startTriangle)
        nameLabel.layoutXProperty().bind(nameLabel.widthProperty().divide(-2))
        nameLabel.layoutY = -12.5
        setOnMouseDragged { event ->
            layoutX = event.sceneX
            layoutY = event.sceneY
        }
    }

    companion object {
        const val radius = 35.0
        private const val shadowRadius = 45.0
        private const val triangleLength = 30.0
        private const val triangleHeight = 20.0
        private const val strokeWidth = 3.0
        private val font = Font("cambria", 24.0)
        private val colorInactive = Color.YELLOW
        private val colorActive = Color.ORANGE
        private val colorShadow = Color.LIGHTGRAY
        private val colorTriangle = Color.GREEN
        private val colorStroke = Color.BLACK
    }

}