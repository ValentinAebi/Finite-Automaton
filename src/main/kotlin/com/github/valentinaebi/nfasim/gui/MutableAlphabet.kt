package com.github.valentinaebi.nfasim.gui

import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.epsilonStr

class MutableAlphabet {
    private val symbols = mutableListOf<Symbol>()
    private val listeners = mutableListOf<GuiTransition>()

    fun setSymbols(newSymbols: List<Symbol>){
        symbols.clear()
        for (symbol in newSymbols){
            symbols.add(symbol)
        }
        invokeOnSymbolsChanged()
    }

    fun addSymbol(symbol: Symbol){
        symbols.add(symbol)
        invokeOnSymbolsChanged()
    }

    fun removeSymbol(symbol: Symbol){
        symbols.remove(symbol)
        invokeOnSymbolsChanged()
    }

    fun addListener(listener: GuiTransition){
        listeners.add(listener)
    }

    fun removeListener(listener: GuiTransition){
        listeners.remove(listener)
    }

    fun getCurrentSymbols(): List<Symbol> = symbols.toList()

    fun containsSymbolMatching(str: String): Boolean = symbols.any { it.toString() == str }

    fun toStringsList(): List<String> = symbols.map { it.toString() }

    fun checkTextSymbolList(str: String): Boolean {
        val spacesRemoved = str.filter { !it.isWhitespace() }
        val split = spacesRemoved.split(symbolsDelimiter)
        val allSymbolsAsStr = getCurrentSymbols().map { it.toString() }
        return spacesRemoved.isEmpty() || (
            split.all{
                allSymbolsAsStr.contains(it) || it == epsilonStr || it.isEmpty()
            } && split.toSet().size == split.size
        )
    }

    private fun invokeOnSymbolsChanged() {
        listeners.forEach { it.handleAlphabetChange() }
    }

    companion object {
        const val symbolsDelimiter = ','
    }

}