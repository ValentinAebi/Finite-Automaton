package com.github.valentinaebi.nfasim.gui

import javafx.beans.binding.Bindings
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import kotlin.math.hypot
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol as Symbol

class GuiTransition(val from: GuiState, val to: GuiState, val triggeringSymbol: Symbol): Group() {

    init {
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
        val labelX = startX.add(endX).divide(2).add(deltaYNormalized.multiply(labelShift)).subtract(5)
        val labelY = startY.add(endY).divide(2).add(deltaXNormalized.multiply(-labelShift)).subtract(10)
        val mainLine = Line()
        mainLine.stroke = color
        mainLine.strokeWidth = strokeWidth
        mainLine.startXProperty().bind(startX)
        mainLine.startYProperty().bind(startY)
        mainLine.endXProperty().bind(endX)
        mainLine.endYProperty().bind(endY)
        val headLine = Line()
        headLine.stroke = color
        headLine.strokeWidth = strokeWidth
        headLine.startXProperty().bind(headX)
        headLine.startYProperty().bind(headY)
        headLine.endXProperty().bind(endX)
        headLine.endYProperty().bind(endY)
        val label = Label(triggeringSymbol.toString())
        label.font = font
        label.layoutXProperty().bind(labelX)
        label.layoutYProperty().bind(labelY)
        children.addAll(mainLine, headLine, label)
    }

    companion object {
        private const val shift = 5.0
        private const val headWidth = 10.0
        private const val headLength = 30.0
        private const val strokeWidth = 5.0
        private const val labelShift = 10.0
        private val font = Font("cambria", 24.0)
        private val color = Color.GREEN
    }

}