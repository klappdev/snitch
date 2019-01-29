package org.kl.parse;

import org.kl.bean.Instruction;
import org.kl.bean.Parameter;

import java.util.Arrays;

public class LineParser {
    private static final String[] LIST_OPERATORS = {
            ">", ">=",
            "<", "<=",
            "!=", "=="
    };

    private static LineParser instance;

    private LineParser() {}

    public static LineParser getInstance() {
        if (instance == null) {
            instance = new LineParser();
        }

        return instance;
    }

    public String[] parseLine(String line) {
        return line.trim().replaceAll(" +", " ").split(" ");
    }

    public boolean isSupportOperations(String operation) {
        return Arrays.stream(LIST_OPERATORS).anyMatch(x -> x.equals(operation));
    }

    public boolean isCorrectValue(String value) {
        return value.matches(".*\\d+.*") || value.equals("null") ||
                value.equals("true")     || value.equals("false");
    }

    public boolean checkArguments(Parameter parameter, Instruction instruction) {
        boolean flag = false;

        switch (instruction.getOperator()) {
        case ">":
            flag = (double) parameter.getValue() > Integer.parseInt(instruction.getValue());
            break;
        case ">=":
            flag = (double) parameter.getValue() >= Integer.parseInt(instruction.getValue());
            break;
        case "<":
            flag = (double) parameter.getValue() < Integer.parseInt(instruction.getValue());
            break;
        case "<=":
            flag = (double) parameter.getValue() <= Integer.parseInt(instruction.getValue());
            break;
        case "!=":
            flag = (double) parameter.getValue() != Integer.parseInt(instruction.getValue());
            break;
        case "==":
            flag = (double) parameter.getValue() == Integer.parseInt(instruction.getValue());
            break;
        }

        return flag;
    }
}
