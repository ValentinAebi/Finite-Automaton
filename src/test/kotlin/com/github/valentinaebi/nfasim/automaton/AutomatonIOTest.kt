package com.github.valentinaebi.nfasim.automaton

import com.github.valentinaebi.nfasim.automaton.TestAutomata.simpleDfa
import com.github.valentinaebi.nfasim.automaton.TestAutomata.simpleNfa
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.Path

class AutomatonIOTest {

    private fun <T>assertEquals(expected: T, actual: T) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual)
    }

    @Test
    fun writeAndThenReadSimpleNfaTest(){
        Files.deleteIfExists(Path(testNfaFileName))
        AutomatonIO.write(simpleNfa, testNfaFileName)
        val read = AutomatonIO.read(testNfaFileName).getOrNull()
        assertEquals(expected = simpleNfa, actual = read)
    }

    @Test
    fun writeAndThenReadSimpleDfaTest(){
        Files.deleteIfExists(Path(testDfaFileName))
        AutomatonIO.write(simpleDfa, testDfaFileName)
        val read = AutomatonIO.read(testDfaFileName).getOrNull()
        assertEquals(expected = simpleDfa, actual = read)
    }

    companion object {
        private const val testDir = "src/test/tmp/"
        private const val testDfaFileName = "${testDir}test_dfa.nfa"
        private const val testNfaFileName = "${testDir}test_nfa.nfa"
    }

}