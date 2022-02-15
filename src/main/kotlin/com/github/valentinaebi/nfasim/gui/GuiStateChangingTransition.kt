package com.github.valentinaebi.nfasim.gui

import javafx.beans.binding.Bindings
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import kotlin.math.hypot

class GuiStateChangingTransition(
        override val from: GuiState,
        override val to: GuiState,
        private val _color: Color,
        override val alphabet: MutableAlphabet,
        override val owner: AutomatonPane
    ): GuiTransition(_color, Line()) {

    private val shadow = shadowAsShape as Line

    init {
        require(from != to) {
            "from and to should differ. GuiSelfTransition should be used for a transition from a state to itself"
        }
        val deltaX = requireNotNull(to.layoutXProperty().subtract(from.layoutXProperty()))
        val deltaY = requireNotNull(to.layoutYProperty().subtract(from.layoutYProperty()))
        val deltaNorm = Bindings.createDoubleBinding({ hypot(deltaX.get(), deltaY.get()) }, deltaX, deltaY)
        val deltaXNormalized = deltaX.divide(deltaNorm)
        val deltaYNormalized = deltaY.divide(deltaNorm)
        val startAndEndShiftX = deltaXNormalized.multiply(GuiState.radius)
        val startAndEndShiftY = deltaYNormalized.multiply(GuiState.radius)
        val startX = from.layoutXProperty().add(deltaYNormalized.multiply(shift)).add(startAndEndShiftX)
        val startY = from.layoutYProperty().add(deltaXNormalized.multiply(-shift)).add(startAndEndShiftY)
        val endX = to.layoutXProperty().add(deltaYNormalized.multiply(shift)).subtract(startAndEndShiftX)
        val endY = to.layoutYProperty().add(deltaXNormalized.multiply(-shift)).subtract(startAndEndShiftY)
        val headX = endX.add(deltaXNormalized.multiply(-headLength)).add(deltaYNormalized.multiply(headWidth))
        val headY = endY.add(deltaYNormalized.multiply(-headLength)).add(deltaXNormalized.multiply(-headWidth))
        val fieldX = startX.multiply(1- fieldToHeadRatio).add(endX.multiply(fieldToHeadRatio)).add(deltaYNormalized.multiply(labelShift)).subtract(5)
        val fieldY = startY.multiply(1- fieldToHeadRatio).add(endY.multiply(fieldToHeadRatio)).add(deltaXNormalized.multiply(-labelShift)).subtract(10)
        val mainLine = Line()
        mainLine.stroke = color
        mainLine.strokeWidth = strokeWidth
        mainLine.startXProperty().bind(startX)
        mainLine.startYProperty().bind(startY)
        mainLine.endXProperty().bind(endX)
        mainLine.endYProperty().bind(endY)
        shadow.startXProperty().bind(startX)
        shadow.startYProperty().bind(startY)
        shadow.endXProperty().bind(endX)
        shadow.endYProperty().bind(endY)
        val headLine = Line()
        headLine.stroke = color
        headLine.strokeWidth = strokeWidth
        headLine.startXProperty().bind(headX)
        headLine.startYProperty().bind(headY)
        headLine.endXProperty().bind(endX)
        headLine.endYProperty().bind(endY)
        triggeringSymbolsField.layoutXProperty().bind(fieldX.subtract(20.0))
        triggeringSymbolsField.layoutYProperty().bind(fieldY.subtract(12.0))
        children.addAll(shadow, mainLine, headLine, triggeringSymbolsField)
    }

    companion object {
        private const val shift = 12.0
        private const val headWidth = 10.0
        private const val headLength = 30.0
        private const val strokeWidth = 5.0
        private const val labelShift = 30.0
        private const val fieldToHeadRatio = 0.45
    }

}