package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import javafx.scene.layout.Pane

class AutomatonPane(val alphabet: MutableAlphabet): Pane() {
    private val states = mutableListOf<GuiState>()
    private val transitions = mutableListOf<GuiTransition>()

    fun add(state: GuiState): Boolean {
        val actuallyAdd = !states.any { it.underlyingState.id == state.underlyingState.id }
        if (actuallyAdd){
            state.layoutX = defaultX
            state.layoutY = defaultY
            states.add(state)
            children.add(state)
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
    }

    fun add(transition: GuiTransition): Boolean {
        val actuallyAdd = !transitions.any { it.from == transition.from && it.to == transition.to }
        if (actuallyAdd){
            transitions.add(transition)
            children.add(transition)
            alphabet.addListener(transition)
        }
        return actuallyAdd
    }

    fun remove(transition: GuiTransition){
        transitions.remove(transition)
        children.remove(transition)
        alphabet.removeListener(transition)
    }

    fun getStates(): List<GuiState> = states.toList()
    fun getTransitions(): List<GuiTransition> = transitions.toList()

    fun buildAutomaton(alphabet: List<Symbol>): FiniteAutomaton {
        return FiniteAutomaton(
            states = states.map { it.underlyingState },
            alphabet = alphabet,
            transitionFunc = transitions.flatMap { tr ->
                tr.getTriggeringSymbols().map { trigSym ->
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

    companion object {
        private const val defaultX = 50.0
        private const val defaultY = 50.0
    }

}