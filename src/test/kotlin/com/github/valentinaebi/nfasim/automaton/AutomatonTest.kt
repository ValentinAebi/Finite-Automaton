package com.github.valentinaebi.nfasim.automaton

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Acceptance
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Epsilon
import com.github.valentinaebi.nfasim.automaton.TestAutomata.a
import com.github.valentinaebi.nfasim.automaton.TestAutomata.b
import com.github.valentinaebi.nfasim.automaton.TestAutomata.c
import com.github.valentinaebi.nfasim.automaton.TestAutomata.q0
import com.github.valentinaebi.nfasim.automaton.TestAutomata.q1
import com.github.valentinaebi.nfasim.automaton.TestAutomata.q2
import com.github.valentinaebi.nfasim.automaton.TestAutomata.q3
import com.github.valentinaebi.nfasim.automaton.TestAutomata.simpleDfa
import com.github.valentinaebi.nfasim.automaton.TestAutomata.simpleNfa
import org.junit.jupiter.api.Test

class AutomatonTest {

    private fun <T>assertEquals(expected: T, actual: T) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual)
    }

    @Test
    fun simpleNfaTransitionsTest(){
        assertEquals(expected = setOf(q1, q2, q3), actual = simpleNfa.transitionFromInitialState(a))
        assertEquals(expected = setOf(q2, q3), actual = simpleNfa.transitionFromInitialState(a, b))
        assertEquals(expected = setOf(q0, q2, q3), actual = simpleNfa.transitionFromInitialState(a, b, c))
        assertEquals(expected = setOf(q1, q2, q3), actual = simpleNfa.transitionFromInitialState(a, b, c, a))
    }

    @Test
    fun simpleDfaTransitionsTest(){
        assertEquals(expected = Acceptance.Accept, actual = simpleDfa.runOn(b, b, a, b, a, a, b, a))
    }

    @Test
    fun simpleNfaIsDfaTest(){
        assertEquals(expected = false, actual = simpleNfa.isDfaState(q0))
        assertEquals(expected = false, actual = simpleNfa.isDfaState(q1))
        assertEquals(expected = false, actual = simpleNfa.isDfaState(q2))
        assertEquals(expected = false, actual = simpleNfa.isDfaState(q3))
        assertEquals(expected = false, actual = simpleNfa.isDfa())
    }

    @Test
    fun simpleDfaIsDfaTest(){
        assertEquals(expected = true, actual = simpleDfa.isDfaState(q0))
        assertEquals(expected = true, actual = simpleDfa.isDfaState(q1))
        assertEquals(expected = true, actual = simpleDfa.isDfaState(q2))
        assertEquals(expected = true, actual = simpleDfa.isDfa())
    }

}
