package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.AutomatonIO
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.epsilonStr
import com.github.valentinaebi.nfasim.gui.MutableAlphabet.Companion.symbolsDelimiter
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlin.math.hypot

class ControlledAutomatonPane(private val ownerWindow: Stage): BorderPane() {
    private val alphabet = MutableAlphabet()
    private val coverPane = Pane()
    val automatonPane = AutomatonPane(alphabet)
    private val controlBar = createControlBar()
    private var currentMode = Mode.Select
    private var partiallyBuiltTransition: Pair<GuiState, Line>? = null
    private val bottomPane = BorderPane()

    var isModifiable = true
        set(_isModifiable){
            field = _isModifiable
            coverPane.isVisible = !isModifiable
            controlBar.isDisable = !isModifiable
        }

    init {
        bottomPane.top = controlBar
        bottomPane.center = automatonPane
        coverPane.isVisible = false
        center = StackPane(bottomPane, coverPane)
    }

    fun setRunPane(runPane: RunPane){
        right = runPane
    }

    fun removeRunPane(){
        right = null
    }

    private fun createControlBar(): Pane {
        val addStateButton = Button("Add state")
        val nameField = TextField()
        addStateButton.onAction = EventHandler {
            if (!automatonPane.getStates().any { it.underlyingState.id == nameField.text }){
                val state = GuiState(State(nameField.text), automatonPane)
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
            automatonPane.removeStates(automatonPane.getStates().filter { it.isSelected })
            automatonPane.removeTransitions(automatonPane.getTransitions().filter { it.isSelected })
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
            if (newVal.any { it.isWhitespace() }){
                alphabetField.text = oldVal
            }
            else {
                val split = newVal.split(symbolsDelimiter)
                if (split.all { it.length == 1 && it[0].isLetterOrDigit() }){
                    alphabet.setSymbols(split.map { Symbol.parse(it) })
                }
                else {
                    alphabetField.text = oldVal
                }
            }
        }
        val typeLabel = Label()
        typeLabel.minWidth = 40.0
        typeLabel.textProperty().bind(
            Bindings.`when`(automatonPane.isMachineProperty).then(
                Bindings.`when`(automatonPane.isDfaProperty).then("DFA").otherwise("NFA")
            ).otherwise("")
        )
        alphabetField.text = defaultAlphabetTextFieldContent
        val alphabetLabel = Label("Alphabet: ")
        val inputLabel = Label("Input: ")
        val inputTextArea = TextArea()
        val runButton = Button("Run")
        inputTextArea.onKeyPressed = EventHandler { event -> if (event.code == KeyCode.ENTER){ runButton.fire() } }
        inputTextArea.textProperty().addListener { _, oldVal, newVal ->
            val split = newVal.filter { !it.isWhitespace() }.split(symbolsDelimiter)
            val formatted = split.map { it.trim() }.filter {
                it.isEmpty() || alphabet.containsSymbolMatching(it) || it == epsilonStr
            }.joinToString(separator = symbolsDelimiter.toString())
            if (newVal != formatted){
                inputTextArea.text = oldVal
            }
        }
        inputTextArea.maxHeight = 80.0
        runButton.onAction = EventHandler {
            if (automatonPane.isMachineProperty.get()){
                runButton.style = "-fx-border: default"
                setRunPane(RunPane(
                    this,
                    inputTextArea.text.split(symbolsDelimiter).filter { it.isNotEmpty() }.map { Symbol.parse(it.trim()) }
                ))
            }
            else {
                runButton.style = "-fx-border-color: red"
            }
        }
        val loadButton = Button("Load automaton")
        loadButton.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(extensionFilter)
            val fileOpt = fileChooser.showOpenDialog(ownerWindow)
            fileOpt?.let { file ->
                AutomatonIO.read(file).onSuccess { automaton ->
                    automatonPane.clear()
                    automaton.states.forEach {
                        val guiState = GuiState(it, automatonPane)
                        guiState.onMouseClicked = EventHandler { event -> if (event.isStillSincePress) onStateClicked(guiState) }
                        automatonPane.add(guiState)
                    }
                    automaton.transitionFunc
                        .map { (startStateAndSymbol, endState) ->
                            Triple(startStateAndSymbol.first, endState, startStateAndSymbol.second)
                        }
                        .groupBy { Pair(it.first, it.second) }
                        .forEach { (_, startEndSymbol) ->
                            val transitionOpt = createTransitionIfNotAlreadyExists(
                                automatonPane.getStates().find { it.underlyingState == startEndSymbol[0].first }!!,
                                automatonPane.getStates().find { it.underlyingState == startEndSymbol[0].second }!!
                            )
                            transitionOpt?.let { tr ->
                                val triggSymbStr = startEndSymbol.map { it.third }.joinToString(separator = ",")
                                val set = tr.setTriggeringSymbols(triggSymbStr)
                                if (set){
                                    automatonPane.add(tr)
                                    tr.toBack()
                                }
                            }
                        }
                    alphabet.setSymbols(automaton.alphabet)
                    automaton.acceptingStates.forEach { state ->
                        automatonPane.getStates().filter { it.underlyingState == state }.forEach { it.isAccepting = true }
                    }
                    automatonPane.getStates().find { it.underlyingState == automaton.initialState }?.let { it.isInit = true }
                }
            }
        }
        val saveButton = Button("Save automaton")
        saveButton.disableProperty().bind(automatonPane.isMachineProperty.not())
        saveButton.onAction = EventHandler {
            val chooser = FileChooser()
            chooser.extensionFilters.add(extensionFilter)
            val fileOpt = chooser.showSaveDialog(ownerWindow)
            fileOpt?.let { file ->
                AutomatonIO.write(automatonPane.buildAutomaton(), file)
            }
        }
        val bar = HBox(VBox(HBox(nameField, addStateButton), HBox(deleteButton, modeChooser)), alphabetLabel,
            alphabetField, typeLabel, inputLabel, inputTextArea, runButton, VBox(loadButton, saveButton))
        bar.alignment = Pos.CENTER_LEFT
        bar.style = "-fx-background-color: lightgray; -fx-spacing: 7;"
        nameField.font = font
        addStateButton.font
        deleteButton.font = font
        inputLabel.font = font
        inputTextArea.font = font
        alphabetLabel.font = font
        alphabetField.font = font
        loadButton.font = font
        saveButton.font = font
        typeLabel.font = Font.font(font.family, FontWeight.BOLD, 20.0)
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
                    line.endX = line.startX + 10
                    line.endY = line.startY
                    onMouseMoved = EventHandler { event ->
                        val localPoint = automatonPane.sceneToLocal(Point2D(event.sceneX, event.sceneY))
                        line.endX = localPoint.x
                        line.endY = localPoint.y
                    }
                    line.onMouseClicked = EventHandler { event ->
                        val localPoint = automatonPane.sceneToLocal(Point2D(event.sceneX, event.sceneY))
                        automatonPane.getStates()
                            .find { hypot(localPoint.x - it.layoutX, localPoint.y - it.layoutY) <= GuiState.radius }
                            ?.fireEvent(event)
                    }
                }
                else {
                    partiallyBuiltTransition?.let { (fromState, line) ->
                        val transition = createTransitionIfNotAlreadyExists(fromState, state)
                        transition?.let {
                            automatonPane.add(it)
                            it.toBack()
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

    private fun createTransitionIfNotAlreadyExists(from: GuiState, to: GuiState): GuiTransition? {
        if (!automatonPane.getTransitions().any { it.from == from && it.to == to }) {
            val transition =
                if (from == to) {
                    GuiSelfTransition(from, colorSelfTransition, alphabet, automatonPane)
                } else {
                    val color =
                        if (automatonPane.getTransitions().any { it.from == to && it.to == from }) colorTransition2
                        else colorTransition1
                    GuiStateChangingTransition(from, to, color, alphabet, automatonPane)
                }
            transition.onMouseClicked = EventHandler { event -> if (event.isStillSincePress) onTransitionClicked(transition) }
            return transition
        }
        else {
            return null
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
        private const val defaultAlphabetTextFieldContent = "0,1"
        private const val partiallyBuiltTransitionLineWidth = 4.0
        private val colorPartiallyBuiltLine = Color.GREEN
        private val colorTransition1 = Color.GREEN
        private val colorTransition2 = Color.BLUE
        private val colorSelfTransition = Color.PURPLE
        private val font = Font("cambria", 14.0)
        private val extensionFilter = FileChooser.ExtensionFilter("Finite automaton files (.dfa)", "*.dfa")
    }

}