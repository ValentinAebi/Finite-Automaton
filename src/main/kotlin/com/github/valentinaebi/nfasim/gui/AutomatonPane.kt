package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import javafx.scene.layout.Pane
import java.lang.IllegalStateException

class AutomatonPane: Pane() {
    private val states = mutableListOf<GuiState>()
    private val transitions = mutableListOf<GuiTransition>()

    fun buildAutomaton(alphabet: List<Symbol>): GuiOption<FiniteAutomaton> {
        val initStates = states.filter { it.isInit }
        if (initStates.isEmpty()){
            return GuiOption.Companion.Message("No initial state selected")
        }
        else if (initStates.size >= 2){
            throw IllegalStateException("more than 1 initial state selected")
        }
        return GuiOption.Companion.Nominal(
            FiniteAutomaton(
                states = states.map { it.underlyingState },
                alphabet = alphabet,
                transitionFunc = transitions.map {
                    Pair(
                        it.from.underlyingState,
                        it.triggeringSymbol
                    ) to it.to.underlyingState
                },
                initialState = initStates[0].underlyingState,
                acceptingStates = states.filter { it.isAccepting }.map { it.underlyingState }.toSet()
            )
        )
    }

}