package com.github.valentinaebi.nfasim.automaton

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Acceptance
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Epsilon
import org.junit.jupiter.api.Test

class AutomatonTest {

    private fun <T>assertEquals(expected: T, actual: T) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual)
    }

    private val q0 = State("q0")
    private val q1 = State("q1")
    private val q2 = State("q2")
    private val q3 = State("q3")

    private val a = Symbol("a")
    private val b = Symbol("b")
    private val c = Symbol("c")

    private val simpleNfa = FiniteAutomaton(
        states = listOf(q0, q1, q2, q3),
        alphabet = listOf(a, b, c),
        transitionFunc = listOf(
            Pair(q0, a) to q1,
            Pair(q0, a) to q3,
            Pair(q0, b) to q2,
            Pair(q2, a) to q3,
            Pair(q2, c) to q3,
            Pair(q1, b) to q3,
            Pair(q3, c) to q0,
            Pair(q3, Epsilon) to q2
        ),
        initialState = q0,
        acceptingStates = setOf(q3)
    )

    private val simpleDfa = FiniteAutomaton(
        states = listOf(q0, q1, q2),
        alphabet = listOf(a, b),
        transitionFunc = listOf(
            Pair(q0, a) to q1,
            Pair(q0, b) to q2,
            Pair(q1, a) to q2,
            Pair(q1, b) to q0,
            Pair(q2, a) to q2,
            Pair(q2, b) to q1
        ),
        initialState = q0,
        acceptingStates = setOf(q2)
    )

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
