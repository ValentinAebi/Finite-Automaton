package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.Pane
import java.lang.IllegalStateException

class AutomatonPane(val alphabet: MutableAlphabet): Pane() {
    private val states = mutableListOf<GuiState>()
    private val transitions = mutableListOf<GuiTransition>()

    private val writableIsDfaProperty = SimpleBooleanProperty()
    private val writableIsMachineProperty = SimpleBooleanProperty()
    val isDfaProperty: ReadOnlyBooleanProperty
        get() = writableIsDfaProperty
    val isMachineProperty: ReadOnlyBooleanProperty
        get() = writableIsMachineProperty

    fun add(state: GuiState): Boolean {
        val actuallyAdd = !states.any { it.underlyingState.id == state.underlyingState.id }
        if (actuallyAdd){
            state.layoutX = defaultX
            state.layoutY = defaultY
            states.add(state)
            children.add(state)
            reportMachineUpdate()
        }
        return actuallyAdd
    }

    fun remove(state: GuiState){
        states.remove(state)
        for (transition in transitions){
            if (transition.from == state || transition.to == state){
                remove(transition)
            }
        }
        children.remove(state)
        reportMachineUpdate()
    }

    fun add(transition: GuiTransition): Boolean {
        val actuallyAdd = !transitions.any { it.from == transition.from && it.to == transition.to }
        if (actuallyAdd){
            transitions.add(transition)
            children.add(transition)
            alphabet.addListener(transition)
            reportMachineUpdate()
        }
        return actuallyAdd
    }

    fun remove(transition: GuiTransition){
        transitions.remove(transition)
        children.remove(transition)
        alphabet.removeListener(transition)
        reportMachineUpdate()
    }

    fun getStates(): List<GuiState> = states.toList()
    fun getTransitions(): List<GuiTransition> = transitions.toList()

    fun buildAutomaton(): FiniteAutomaton {
        if (transitions.any { it.currentText().isEmpty() }){
            throw IllegalStateException("cannot build automaton: at least one transition has no triggering symbol")
        }
        return FiniteAutomaton(
            states = states.map { it.underlyingState },
            alphabet = alphabet.getCurrentSymbols(),
            transitionFunc = transitions.flatMap { tr ->
                tr.getTriggeringSymbols().filter { it.toString().isNotEmpty() }.map { trigSym ->
                    Pair(
                        tr.from.underlyingState,
                        trigSym,
                    ) to tr.to.underlyingState
                 }
            },
            initialState = states.find { it.isInit }!!.underlyingState,
            acceptingStates = states.filter { it.isAccepting }.map { it.underlyingState }.toSet()
        )
    }

    fun reportMachineUpdate() {
        val hasStartState = getStates().any { it.isInit }
        val allTransitionsDefined = getTransitions().all { it.currentText().isNotEmpty() }
        val isMachine = hasStartState && allTransitionsDefined
        writableIsMachineProperty.set(isMachine)
        writableIsDfaProperty.set(isMachine && buildAutomaton().isDfa())
    }

    companion object {
        private const val defaultX = 50.0
        private const val defaultY = 50.0
    }

}