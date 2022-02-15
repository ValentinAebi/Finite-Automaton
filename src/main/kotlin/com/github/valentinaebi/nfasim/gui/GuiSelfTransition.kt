package com.github.valentinaebi.nfasim.gui

import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line

class GuiSelfTransition(
    val state: GuiState,
    private val _color: Color,
    override val alphabet: MutableAlphabet,
    override val owner: AutomatonPane
) : GuiTransition(_color, Circle()) {

    private val shadow = shadowAsShape as Circle

    init {
        val mainCircle = Circle(transitionCircleRadius)
        mainCircle.centerXProperty().bind(state.layoutXProperty())
        mainCircle.centerYProperty().bind(state.layoutYProperty().subtract(GuiState.radius))
        mainCircle.stroke = color
        mainCircle.strokeWidth = strokeWidth
        mainCircle.fill = Color.TRANSPARENT
        val leftHeadLine = Line()
        val rightHeadLine = Line()
        leftHeadLine.startXProperty().bind(state.layoutXProperty().subtract(27.5))
        leftHeadLine.startYProperty().bind(state.layoutYProperty().subtract(23.0))
        leftHeadLine.endXProperty().bind(leftHeadLine.startXProperty().subtract(12.0))
        leftHeadLine.endYProperty().bind(leftHeadLine.startYProperty().subtract(9.0))
        rightHeadLine.startXProperty().bind(leftHeadLine.startXProperty())
        rightHeadLine.startYProperty().bind(leftHeadLine.startYProperty())
        rightHeadLine.endXProperty().bind(rightHeadLine.startXProperty().add(9.0))
        rightHeadLine.endYProperty().bind(rightHeadLine.startYProperty().subtract(12.0))
        leftHeadLine.stroke = color
        leftHeadLine.strokeWidth = strokeWidth
        rightHeadLine.stroke = color
        rightHeadLine.strokeWidth = strokeWidth
        triggeringSymbolsField.layoutXProperty().bind(state.layoutXProperty().subtract(40.0))
        triggeringSymbolsField.layoutYProperty().bind(state.layoutYProperty().subtract(2 * transitionCircleRadius + 40.0))
        shadow.isVisible = false
        shadow.centerXProperty().bind(mainCircle.centerXProperty())
        shadow.centerYProperty().bind(mainCircle.centerYProperty())
        shadow.radius = transitionCircleRadius
        shadow.fill = Color.TRANSPARENT
        children.addAll(shadow, mainCircle, leftHeadLine, rightHeadLine, triggeringSymbolsField)
    }

    override val from: GuiState
        get() = state

    override val to: GuiState
        get() = state

    companion object {
        private const val transitionCircleRadius = 30.0
        private const val strokeWidth = 3.0
    }

}