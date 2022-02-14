package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import kotlin.math.hypot

class GuiTransition(val from: GuiState, val to: GuiState, val color: Color, val alphabet: List<Symbol>): Group() {
    private val allSymbolsAsStr = alphabet.map { it.toString() }

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
        val fieldX = startX.multiply(1- fieldToHeadRatio).add(endX.multiply(fieldToHeadRatio)).add(deltaYNormalized.multiply(labelShift)).subtract(5)
        val fieldY = startY.multiply(1- fieldToHeadRatio).add(endY.multiply(fieldToHeadRatio)).add(deltaXNormalized.multiply(-labelShift)).subtract(10)
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
        val triggeringSymbolsField = TextField()
        triggeringSymbolsField.font = font
        triggeringSymbolsField.prefColumnCount = 10
        triggeringSymbolsField.layoutXProperty().bind(fieldX.subtract(40.0))
        triggeringSymbolsField.layoutYProperty().bind(fieldY.subtract(12.0))
        triggeringSymbolsField.style = "-fx-text-inner-color: #${color.toString().drop(2)}; -fx-background-color: rgba(100, 100, 100, 0.1)"
        triggeringSymbolsField.alignment = Pos.CENTER
        triggeringSymbolsField.textProperty().addListener { _, oldVal, newVal ->
            if (!checkTriggeringSymbolsField(newVal)) triggeringSymbolsField.text = oldVal
        }
        onMouseClicked = EventHandler { triggeringSymbolsField.requestFocus() }
        children.addAll(mainLine, headLine, triggeringSymbolsField)
    }

    private fun checkTriggeringSymbolsField(str: String): Boolean {
        val spacesRemoved = str.filter { !it.isWhitespace() }
        val split = spacesRemoved.split(',')
        return spacesRemoved.isEmpty() || (split.all{ allSymbolsAsStr.contains(it) || it.isEmpty() } && split.toSet().size == split.size)
    }


    companion object {
        private const val shift = 12.0
        private const val headWidth = 10.0
        private const val headLength = 30.0
        private const val strokeWidth = 5.0
        private const val labelShift = 30.0
        private const val fieldToHeadRatio = 0.45
        private val font = Font("cambria", 14.0)
    }

}