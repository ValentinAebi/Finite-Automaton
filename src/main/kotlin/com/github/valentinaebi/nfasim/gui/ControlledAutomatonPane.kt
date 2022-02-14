package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Line

class ControlledAutomatonPane: BorderPane() {
    private val alphabet = MutableAlphabet()
    private val automatonPane = AutomatonPane(alphabet)
    private val currentMode = Mode.Select
    private var partiallyBuiltTransition: Pair<GuiState, Line>? = null

    init {
        top = createControlBox()
        center = automatonPane
    }

    private fun createControlBox(): Pane {
        val addStateButton = Button("Add state")
        val nameField = TextField()
        addStateButton.onAction = EventHandler {
            val state = GuiState(State(nameField.text), automatonPane)
            state.onMouseClicked = EventHandler { onStateClicked(state) }
            automatonPane.add(state)
        }
        addStateButton.disableProperty().bind(Bindings.createBooleanBinding({ nameField.text.isEmpty() }, nameField.textProperty()))
        val deleteButton = Button("Delete")
        deleteButton.onAction = EventHandler {
            automatonPane.getStates().filter { it.isSelected }.forEach(automatonPane::remove)
            automatonPane.getTransitions().filter { it.isSelected }.forEach(automatonPane::remove)
        }
        val modeChooser = ChoiceBox<Mode>()
        modeChooser.items.addAll(Mode.values())
        modeChooser.selectionModel.selectedIndexProperty().addListener { _, oldIdx, newIdx ->
            if (oldIdx != -1){
                onModeTearDown(modeChooser.items[oldIdx!!.toInt()])
            }
            onModeSetup(modeChooser.items[newIdx!!.toInt()])
        }
        val alphabetField = TextField()
        alphabetField.textProperty().addListener { _, oldVal, newVal ->
            if (!newVal.split(MutableAlphabet.symbolsDelimiter)
                    .all {
                        s -> s.all { it.isLetterOrDigit() || it == '_' }
                    }){
                alphabetField.text = oldVal
            }
        }
        val alphabetLabel = Label("Alphabet: ")
        val bar = HBox(nameField, addStateButton, deleteButton, modeChooser, alphabetLabel, alphabetField)
        bar.alignment = Pos.CENTER_LEFT
        bar.style = "-fx-background-color: lightgray; -fx-spacing: 5;"
        return bar
    }

    private fun onModeSetup(mode: Mode){
        when (mode){
            Mode.Select -> {
                automatonPane.getStates().forEach { it.canMove = true }
            }
            Mode.CreateTransition -> { }
        }
    }

    private fun onModeTearDown(mode: Mode){
        when (mode){
            Mode.Select -> {
                automatonPane.getStates().forEach { it.isSelected = false; it.canMove = false }
            }
            Mode.CreateTransition -> {
                partiallyBuiltTransition?.let { automatonPane.children.remove(it.first) }
                partiallyBuiltTransition = null
            }
        }
    }

    private fun onStateClicked(state: GuiState){
        when (currentMode){
            Mode.Select -> {
                state.isSelected = !state.isSelected
            }
            Mode.CreateTransition -> {
                if (partiallyBuiltTransition == null){
                    val line = Line()
                    partiallyBuiltTransition = Pair(state, line)
                    children.add(line)
                }
                else {
                    partiallyBuiltTransition?.let {
                        val transition = GuiTransition(it.first, state, Color.BLUE, alphabet)
                        transition.onMouseClicked = EventHandler { onTransitionClicked(transition) }
                        automatonPane.add(transition)
                    }
                }
            }
        }
    }

    private fun onTransitionClicked(transition: GuiTransition){
        when (currentMode){
            Mode.Select -> {
                transition.isSelected = !transition.isSelected
            }
            Mode.CreateTransition -> { }
        }
    }

    companion object {
        private enum class Mode {
            Select, CreateTransition
        }
    }

}