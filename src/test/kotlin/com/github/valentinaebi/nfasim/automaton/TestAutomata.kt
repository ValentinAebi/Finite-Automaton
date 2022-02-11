package com.github.valentinaebi.nfasim.automaton

object TestAutomata {

    val q0 = FiniteAutomaton.Companion.State("q0")
    val q1 = FiniteAutomaton.Companion.State("q1")
    val q2 = FiniteAutomaton.Companion.State("q2")
    val q3 = FiniteAutomaton.Companion.State("q3")

    val a = FiniteAutomaton.Symbol("a")
    val b = FiniteAutomaton.Symbol("b")
    val c = FiniteAutomaton.Symbol("c")

    val simpleNfa = FiniteAutomaton(
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
            Pair(q3, FiniteAutomaton.Companion.Epsilon) to q2
        ),
        initialState = q0,
        acceptingStates = setOf(q3)
    )

    val simpleDfa = FiniteAutomaton(
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

}