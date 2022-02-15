package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import com.github.valentinaebi.nfasim.gui.MutableAlphabet.Companion.symbolsDelimiter
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.scene.text.Font

abstract class GuiTransition(val color: Color, val shadowAsShape: Shape): Group() {

    protected val triggeringSymbolsField = TextField()

    abstract val from: GuiState
    abstract val to: GuiState
    abstract val alphabet: MutableAlphabet

    var isSelected = false
        set(_isSelected){
            field = _isSelected
            shadowAsShape.isVisible = isSelected
        }

    init {
        triggeringSymbolsField.font = font
        triggeringSymbolsField.prefColumnCount = 5
        triggeringSymbolsField.style = "-fx-text-inner-color: #${color.toString().drop(2)}; -fx-background-color: rgba(100, 100, 100, 0.1)"
        triggeringSymbolsField.alignment = Pos.CENTER
        triggeringSymbolsField.textProperty().addListener { _, oldVal, newVal ->
            if (!alphabet.checkTextSymbolList(newVal)) triggeringSymbolsField.text = oldVal
        }
        onMouseClicked = EventHandler { triggeringSymbolsField.requestFocus() }
        shadowAsShape.stroke = colorShadow
        shadowAsShape.strokeWidth = shadowWidth
        shadowAsShape.isVisible = false
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
        private val font = Font("cambria", 14.0)
        private const val shadowWidth = 12.0
        private val colorShadow = Color.GRAY
    }

}