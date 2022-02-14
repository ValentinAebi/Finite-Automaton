package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import javafx.scene.layout.Pane

class AutomatonPane(val alphabet: MutableAlphabet): Pane() {
    private val states = mutableListOf<GuiState>()
    private val transitions = mutableListOf<GuiTransition>()
    private var initState: GuiState? = null

    fun add(state: GuiState): Boolean {
        val actuallyAdd = !states.any { it.underlyingState.id == state.underlyingState.id }
        if (actuallyAdd){
            states.add(state)
            children.add(state)
        }
        return actuallyAdd
    }

    fun remove(state: GuiState){
        states.remove(state)
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

    fun setInitState(newInitState: GuiState){
        require(states.contains(newInitState))
        initState?.isInit = false
        initState = newInitState
    }

    fun buildAutomaton(alphabet: List<Symbol>): FiniteAutomaton {
        val confirmedInitState = requireNotNull(initState) { throw IllegalStateException("cannot build automaton: no initial state selected") }
        return FiniteAutomaton(
            states = states.map { it.underlyingState },
            alphabet = alphabet,
            transitionFunc = transitions.map {
                Pair(
                    it.from.underlyingState,
                    TODO()//it.triggeringSymbol
                ) to it.to.underlyingState
            },
            initialState = confirmedInitState.underlyingState,
            acceptingStates = states.filter { it.isAccepting }.map { it.underlyingState }.toSet()
        )
    }

}