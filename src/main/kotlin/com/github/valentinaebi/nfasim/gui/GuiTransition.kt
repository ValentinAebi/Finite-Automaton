package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import com.github.valentinaebi.nfasim.gui.MutableAlphabet.Companion.symbolsDelimiter
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import java.lang.IllegalStateException
import kotlin.math.hypot

class GuiTransition(val from: GuiState, val to: GuiState, val color: Color, val alphabet: MutableAlphabet): Group() {

    var isSelected = false
        set(_isSelected){
            field = _isSelected
            shadow.isVisible = isSelected
        }

    private val allSymbolsAsStr = alphabet.toStringsList()
    private val triggeringSymbolsField = TextField()
    private val shadow = Line()

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
        shadow.stroke = colorShadow
        shadow.strokeWidth = shadowWidth
        shadow.startXProperty().bind(startX)
        shadow.startYProperty().bind(startY)
        shadow.endXProperty().bind(endX)
        shadow.endYProperty().bind(endY)
        shadow.isVisible = false
        val headLine = Line()
        headLine.stroke = color
        headLine.strokeWidth = strokeWidth
        headLine.startXProperty().bind(headX)
        headLine.startYProperty().bind(headY)
        headLine.endXProperty().bind(endX)
        headLine.endYProperty().bind(endY)
        triggeringSymbolsField.font = font
        triggeringSymbolsField.prefColumnCount = 10
        triggeringSymbolsField.layoutXProperty().bind(fieldX.subtract(40.0))
        triggeringSymbolsField.layoutYProperty().bind(fieldY.subtract(12.0))
        triggeringSymbolsField.style = "-fx-text-inner-color: #${color.toString().drop(2)}; -fx-background-color: rgba(100, 100, 100, 0.1)"
        triggeringSymbolsField.alignment = Pos.CENTER
        triggeringSymbolsField.textProperty().addListener { _, oldVal, newVal ->
            if (!alphabet.checkTextSymbolList(newVal)) triggeringSymbolsField.text = oldVal
        }
        onMouseClicked = EventHandler { triggeringSymbolsField.requestFocus() }
        children.addAll(mainLine, headLine, triggeringSymbolsField)
    }

    fun getTriggeringSymbols(): List<Symbol> {
        val text = triggeringSymbolsField.text
        if (!alphabet.checkTextSymbolList(text)){
            throw IllegalStateException("invalid specification of triggering symbols")
        }
        return text.split(symbolsDelimiter).map { Symbol(it) }
    }

    fun handleAlphabetChange(){
        val newText = triggeringSymbolsField.text
            .split(symbolsDelimiter)
            .filter { alphabet.containsSymbolMatching(it) }
            .joinToString()
        triggeringSymbolsField.text = newText
    }

    companion object {
        private const val shift = 12.0
        private const val headWidth = 10.0
        private const val headLength = 30.0
        private const val strokeWidth = 5.0
        private const val labelShift = 30.0
        private const val fieldToHeadRatio = 0.45
        private const val shadowWidth = 12.0
        private val font = Font("cambria", 14.0)
        private val colorShadow = Color.GRAY
    }

}