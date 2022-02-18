package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Font

class RunPane(val controlledAutomatonPane: ControlledAutomatonPane, val input: List<Symbol>): BorderPane() {
    private val automatonPane = controlledAutomatonPane.automatonPane
    private val automaton = automatonPane.buildAutomaton()
    private val successiveActiveStates = input.fold(
        listOf(automatonPane.getStates().filter { it.isInit }.map { it.underlyingState }.toSet())
    ) {
            acc: List<Set<State>>, symbol: Symbol -> acc + listOf(automaton.transition(acc.last(), symbol))
    }
    private val guiStatesFromStates = automatonPane.getStates().associateBy { it.underlyingState }
    private val currentIterLabel = Label()
    private val inputRepres = input.map { Label(it.toString()) }

    private var currentActiveStatesIdx = 0
        set(_currentActiveStatesIdx){
            field = minOf(maxOf(_currentActiveStatesIdx, 0), successiveActiveStates.size-1)
            currentIterLabel.text = currentActiveStatesIdx.toString()
            update()
        }

    init {
        minWidth = 400.0
        style = "-fx-background-color: lightblue; -fx-border-color: black;"
        controlledAutomatonPane.isModifiable = false
        top = createInputRepresentation()
        center = createControlBar()
        inputRepres.forEach { it.font = font }
        currentIterLabel.font = font
        currentIterLabel.minWidth = 20.0
        update()
        requestFocus()
    }

    private fun update() {
        setAllStatesToInactive()
        successiveActiveStates[currentActiveStatesIdx].forEach { guiStatesFromStates[it]!!.isActive = true }
        inputRepres.forEach { it.style = "-fx-background: default;" }
        if (inputRepres.isNotEmpty() && currentActiveStatesIdx >= 1){
            inputRepres[currentActiveStatesIdx-1].style = "-fx-background-color: orange;"
        }
    }

    private fun createInputRepresentation(): Pane {
        val vBox = VBox()
        vBox.children.addAll(inputRepres.chunked(maxSymbolsPerLine).map { chunk ->
            val hBox = HBox()
            hBox.children.addAll(chunk)
            hBox.style = "-fx-spacing: 3"
            hBox
        })
        return vBox
    }

    private fun createControlBar(): Pane {
        val oneStepForwardButton = Button(">")
        oneStepForwardButton.font = font
        oneStepForwardButton.onAction = EventHandler { currentActiveStatesIdx += 1 }
        val oneStepBackwardButton = Button("<")
        oneStepBackwardButton.font = font
        oneStepBackwardButton.onAction = EventHandler { currentActiveStatesIdx -= 1 }
        val closeRunButton = Button("X")
        closeRunButton.font = font
        closeRunButton.onAction = EventHandler { onClose() }
        val iterTextLabel = Label("Current iteration: ")
        iterTextLabel.font = font
        val bar = HBox(oneStepBackwardButton, oneStepForwardButton, iterTextLabel, currentIterLabel, closeRunButton)
        return bar
    }

    private fun onClose(){
        controlledAutomatonPane.isModifiable = true
        controlledAutomatonPane.removeRunPane()
        setAllStatesToInactive()
    }

    private fun setAllStatesToInactive() {
        automatonPane.getStates().forEach { it.isActive = false }
    }

    companion object {
        private const val maxSymbolsPerLine = 8
        private val font = Font("cambria", 14.0)
    }

}