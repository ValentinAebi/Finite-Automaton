package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Line

class ControlledAutomatonPane: BorderPane() {
    private val alphabet = MutableAlphabet()
    private val automatonPane = AutomatonPane(alphabet)
    private var currentMode = Mode.Select
    private var partiallyBuiltTransition: Pair<GuiState, Line>? = null

    init {
        top = createControlBox()
        center = automatonPane
    }

    private fun createControlBox(): Pane {
        val addStateButton = Button("Add state")
        val nameField = TextField()
        addStateButton.onAction = EventHandler {
            if (!automatonPane.getStates().any { it.underlyingState.id == nameField.text }){
                val state = GuiState(State(nameField.text))
                state.onMouseClicked = EventHandler { event -> if (event.isStillSincePress) onStateClicked(state) }
                automatonPane.add(state)
                nameField.text = ""
            }
        }
        addStateButton.disableProperty().bind(Bindings.createBooleanBinding({ nameField.text.isEmpty() }, nameField.textProperty()))
        nameField.onKeyPressed = EventHandler { event -> if (event.code == KeyCode.ENTER) addStateButton.fire() }
        nameField.textProperty().addListener { _, oldValue, newValue ->
            if (!newValue.all { it.isLetterOrDigit() || it == '_' }) nameField.text = oldValue
            nameField.style =
                if (automatonPane.getStates().any { it.underlyingState.id == nameField.text }) "-fx-border-color: red"
                else "-fx-border: default"
        }
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
            currentMode = modeChooser.items[newIdx!!.toInt()]
            onModeSetup(currentMode)
        }
        modeChooser.selectionModel.select(Mode.Select)
        val alphabetField = TextField()
        alphabetField.textProperty().addListener { _, oldVal, newVal ->
            val split = newVal.filter { !it.isWhitespace() }.split(MutableAlphabet.symbolsDelimiter)
            if (split.all {
                        s -> s.all { it.isLetterOrDigit() || it == '_' || it.isWhitespace() }
            }){
                alphabet.setSymbols(split.map { Symbol(it) })
            }
            else {
                alphabetField.text = oldVal
            }
        }
        alphabetField.text = defaultAlphabetTextFieldContent
        val alphabetLabel = Label("Alphabet: ")
        val bar = HBox(nameField, addStateButton, deleteButton, modeChooser, alphabetLabel, alphabetField)
        bar.alignment = Pos.CENTER_LEFT
        bar.style = "-fx-background-color: lightgray; -fx-spacing: 5;"
        return bar
    }

    private fun onModeSetup(mode: Mode){
        when (mode){
            Mode.Select -> { }
            Mode.CreateTransition -> { }
            Mode.MarkAccept -> { }
            Mode.MarkInit -> { }
        }
    }

    private fun onModeTearDown(mode: Mode){
        when (mode){
            Mode.Select -> {
                automatonPane.getStates().forEach { it.isSelected = false }
            }
            Mode.CreateTransition -> {
                partiallyBuiltTransition?.let { automatonPane.children.remove(it.second) }
                partiallyBuiltTransition = null
                onMouseMoved = null
            }
            Mode.MarkAccept -> { }
            Mode.MarkInit -> { }
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
                    line.stroke = colorPartiallyBuiltLine
                    line.strokeWidth = partiallyBuiltTransitionLineWidth
                    partiallyBuiltTransition = Pair(state, line)
                    automatonPane.children.add(line)
                    line.startXProperty().bind(state.layoutXProperty())
                    line.startYProperty().bind(state.layoutYProperty())
                    onMouseMoved = EventHandler { event -> line.endX = event.sceneX ; line.endY = event.sceneY }
                }
                else {
                    partiallyBuiltTransition?.let { (fromState, line) ->
                        if (!automatonPane.getTransitions().any { it.from == fromState && it.to == state }){
                            val transition =
                                if (fromState == state) {
                                    GuiSelfTransition(fromState, colorSelfTransition, alphabet)
                                }
                                else {
                                    val color =
                                        if (automatonPane.getTransitions().any { it.from == state && it.to == fromState }) colorTransition2
                                        else colorTransition1
                                    GuiStateChangingTransition(fromState, state, color, alphabet)
                                }
                            transition.onMouseClicked = EventHandler { event -> if (event.isStillSincePress) onTransitionClicked(transition) }
                            automatonPane.add(transition)
                            transition.toBack()
                        }
                        automatonPane.children.remove(line)
                    }
                    partiallyBuiltTransition = null
                    onMouseMoved = null
                }
            }
            Mode.MarkAccept -> {
                state.isAccepting = !state.isAccepting
            }
            Mode.MarkInit -> {
                state.isInit = !state.isInit
                automatonPane.getStates().filter { it != state }.forEach { it.isInit = false }
            }
        }
    }

    private fun onTransitionClicked(transition: GuiTransition){
        when (currentMode){
            Mode.Select -> {
                transition.isSelected = !transition.isSelected
            }
            Mode.CreateTransition -> { }
            Mode.MarkAccept -> { }
            Mode.MarkInit -> { }
        }
    }

    companion object {
        private enum class Mode {
            Select, CreateTransition, MarkAccept, MarkInit
        }
        private const val defaultAlphabetTextFieldContent = "0, 1"
        private const val partiallyBuiltTransitionLineWidth = 4.0
        private val colorPartiallyBuiltLine = Color.GREEN
        private val colorTransition1 = Color.GREEN
        private val colorTransition2 = Color.BLUE
        private val colorSelfTransition = Color.PURPLE
    }

}