package com.mathpuzzlegame.model;

public class GameQuestion {
    private final String expression;
    private final int answer;

    public GameQuestion(String expression, int answer) {
        this.expression = expression;
        this.answer = answer;
    }

    public String getExpression() {
        return expression;
    }

    public int getAnswer() {
        return answer;
    }
}