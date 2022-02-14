package com.github.valentinaebi.nfasim.automaton

import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.State.Companion as State
import com.github.valentinaebi.nfasim.automaton.FiniteAutomaton.Companion.Symbol.Companion as Symbol

object AutomatonIO {

    private const val arrow: String = "->"
    private const val subsectionIndicator: String = "$"
    private const val concat: String = ","
    private const val tagOpening: String = "["
    private const val tagClosing: String = "]"
    private const val commentMarker: String = "#"

    fun write(automaton: FiniteAutomaton, fileName: String) {

        fun <T>addSubsection(sb: StringBuilder, ls: Collection<T>, tag: Tag) {
            sb.append(tag.surrounded())
            sb.append("\n")
            for (elem in ls){
                sb.append(subsectionIndicator)
                sb.append(elem)
                sb.append("\n")
            }
        }

        val sb = StringBuilder()
        addSubsection(sb, automaton.states, Tag.States)
        addSubsection(sb, automaton.alphabet, Tag.Alphabet)
        addSubsection(sb, automaton.transitionFunc.map { (k, v) -> "${k.first}$concat${k.second}$arrow$v" }, Tag.Transitions)
        addSubsection(sb, listOf(automaton.initialState), Tag.Start)
        addSubsection(sb, automaton.acceptingStates, Tag.Accept)
        sb.append("\n")
        val str = sb.toString()
        FileWriter(fileName).use { it.write(str) }
    }

    fun read(fileName: String): Result<FiniteAutomaton> {

        fun requireFormat(cond: Boolean, msg: String) {
            if (!cond) throw IOException("illegal format: $msg")
        }

        fun readSubsection(lines: List<String>, startIdx: Int, tag: Tag): Pair<List<String>, Int> {
            requireFormat(lines[startIdx] == tag.surrounded(), "$tag marker")
            val subsectionLines = lines.drop(startIdx+1).takeWhile { it.startsWith(subsectionIndicator) }
            val nextIdx = startIdx + subsectionLines.size
            return Pair(subsectionLines, nextIdx)
        }

        try {
            val lines = FileReader(fileName).use {
                it.readLines()
            }
                .map { it.dropWhile(Char::isWhitespace).dropLastWhile(Char::isWhitespace) }
                .filter { it.isNotEmpty() && !it.startsWith(commentMarker) }

            requireFormat(lines.isNotEmpty(), "empty file or all lines are empty")
            val (stateLines, lastStateIdx) = readSubsection(lines, 0, Tag.States)
            val (alphabetLines, lastAlphabetSymbolIdx) = readSubsection(lines, lastStateIdx+1, Tag.Alphabet)
            val (transitionLines, lastTransitionIdx) = readSubsection(lines, lastAlphabetSymbolIdx+1, Tag.Transitions)
            val (initStateLines, lastInitStateIdx) = readSubsection(lines, lastTransitionIdx+1, Tag.Start)
            requireFormat(initStateLines.size == 1, "found 0 or more than 1 starting state")
            val (acceptStatesLines, lastAcceptIdx) = readSubsection(lines, lastInitStateIdx+1, Tag.Accept)
            requireFormat(lastAcceptIdx == lines.size-1, "unexpected line(s) at the end of the file")
            val states = stateLines.map { State.parse(it.drop(subsectionIndicator.length)) }
            val alphabet = alphabetLines.map { Symbol.parse(it.drop(subsectionIndicator.length)) }
            val transitions = transitionLines.map {
                val sp1 = it.drop(subsectionIndicator.length).split(concat, ignoreCase = false, limit = 2)
                requireFormat(sp1.size == 2, "transition $it")
                val sp2 = sp1[1].split(arrow, ignoreCase = false, limit = 2)
                requireFormat(sp2.size == 2, "transition $it")
                val inputState = State.parse(sp1[0])
                val inputSymbol = Symbol.parse(sp2[0])
                val outputState = State.parse(sp2[1])
                Pair(inputState, inputSymbol) to outputState
            }
            val initState = State.parse(initStateLines[0].drop(subsectionIndicator.length))
            val acceptStates = acceptStatesLines.map { State.parse(it.drop(subsectionIndicator.length)) }
            return Result.success(FiniteAutomaton(states, alphabet, transitions, initState, acceptStates.toSet()))
        } catch (e: Exception){
            return Result.failure(e)
        }
    }

    enum class Tag {
        States, Alphabet, Transitions, Start, Accept;
        fun surrounded(): String = "$tagOpening$this$tagClosing"
    }

}