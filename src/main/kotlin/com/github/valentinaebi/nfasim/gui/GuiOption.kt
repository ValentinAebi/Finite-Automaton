package com.github.valentinaebi.nfasim.gui

interface GuiOption<T> {

    companion object {
        data class Nominal<U>(val value: U): GuiOption<U>
        data class Message<U>(val msg: String): GuiOption<U>
    }

}