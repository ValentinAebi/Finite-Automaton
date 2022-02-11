package com.github.valentinaebi.nfasim.automaton


data class FiniteAutomaton(
    val states: List<State>,
    val alphabet: List<Symbol>,
    val transitionFunc: List<Pair<Pair<State, Symbol>, State>>,
    val initialState: State,
    val acceptingStates: Set<State>
) {

    init {
        require(states.isNotEmpty()) { "a finite automaton must have at least 1 state" }
        require(alphabet.isNotEmpty()){ "a finite automaton cannot have an empty alphabet" }
        require(states.containsAll(transitionFunc.map{ it.first.first })){
            "input states given in the transition function must be states of the automaton"
        }
        require(alphabet.containsAll(transitionFunc.filter{ it.first.second != Epsilon }.map{ it.first.second })){
            "input symbols given in the transition function must be in the alphabet"
        }
        require(states.containsAll(transitionFunc.map{ it.second })){
            "output states given in the transition function must be states of the automaton"
        }
        require(states.contains(initialState)){ "initial state must be a state of the automaton" }
        require(states.containsAll(acceptingStates)){
            "set of accepting states must be a subset of the states of the automaton"
        }
    }

    private fun explicitTransitionOnce(initiallyActiveStates: Set<State>, symbol: Symbol) =
        transitionFunc
            .filter { (key, _) ->
                key.second == symbol && initiallyActiveStates.contains(key.first)
            }.map { it.second }.toSet()

    private fun transitionOnce(initiallyActiveStates: Set<State>, symbol: Symbol): Set<State> {
        val regularTransitionsResult = explicitTransitionOnce(initiallyActiveStates, symbol)
        return regularTransitionsResult.union(explicitTransitionOnce(regularTransitionsResult, Epsilon))
    }

    fun transition(initiallyActiveStates: Set<State>, symbols: List<Symbol>): Set<State> =
        symbols.fold(initiallyActiveStates){ acc, symbol -> transitionOnce(acc, symbol) }

    fun transition(initiallyActiveStates: Set<State>, vararg symbols: Symbol): Set<State> =
        transition(initiallyActiveStates, symbols.toList())

    fun transitionFromInitialState(symbols: List<Symbol>): Set<State> = transition(setOf(initialState), symbols)

    fun transitionFromInitialState(vararg symbols: Symbol): Set<State> = transitionFromInitialState(symbols.toList())

    fun runOn(input: List<Symbol>): Acceptance {
        val endStates = transition(setOf(initialState), input)
        return if (endStates.intersect(acceptingStates).isNotEmpty()) Acceptance.Accept else Acceptance.Reject
    }

    fun runOn(vararg symbols: Symbol): Acceptance = runOn(symbols.toList())

    fun isDfaState(state: State): Boolean {
        require(states.contains(state)){ "cannot check state that is not registered" }
        val asSet = setOf(state)
        return alphabet.all { symbol -> explicitTransitionOnce(asSet, symbol).size == 1 }
                && explicitTransitionOnce(asSet, Epsilon).isEmpty()
    }

    fun isDfa(): Boolean = states.all { isDfaState(it) }

    companion object {

        private const val epsilonStr: String = "~"

        fun Symbol(repres: String): Symbol = TrueSymbol(repres)
        sealed interface Symbol {
            companion object {
                fun parse(repres: String): Symbol = when(repres) {
                    epsilonStr -> Epsilon
                    else -> Symbol(repres)
                }
            }
        }
        private data class TrueSymbol(private val repres: String): Symbol {
            init { checkIsValidId(repres) }
            override fun toString(): String = repres
        }
        /** Empty transition */
        object Epsilon: Symbol {
            override fun toString(): String = epsilonStr
        }

        data class State(val id: String){
            init { checkIsValidId(id) }
            override fun toString(): String = id
            companion object {
                fun parse(id: String): State = State(id)
            }
        }

        enum class Acceptance { Accept, Reject }

        private fun checkIsValidId(str: String) = require(str.all { it.isLetterOrDigit() || it == '_' }) {
            "'$str' is not a valid identifier: only letters, digits and underscores are allowed"
        }
    }

}