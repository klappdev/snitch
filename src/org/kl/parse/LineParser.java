package org.kl.parse;

import org.kl.bean.Instruction;
import org.kl.bean.Parameter;
import org.kl.error.ContractException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class LineParser {
    private final String[] LIST_OPERATORS = {
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

    public List<Instruction> parseLine(String line) throws ContractException {
        List<Instruction> list = new ArrayList<>();

        if (line.contains("||") || line.contains("&&")) {
            String[] parts = line.trim().replaceAll(" +", " ").split("(\\|\\|)|(&&)");

            for (String part : parts) {
                String[] pieces = part.trim().split(" ");

                if (pieces.length != 3) {
                    throw new ContractException("Correct instruction: left operator right");
                }

                list.add(new Instruction(pieces[0], pieces[1], pieces[2]));
            }
        } else {
            String[] pieces = line.trim().replaceAll(" +", " ").split(" ");

            if (pieces.length != 3) {
                throw new ContractException("Correct instruction: left operator right");
            }

            list.add(new Instruction(pieces[0], pieces[1], pieces[2]));
        }

        return list;
    }

    public boolean checkParameters(List<Parameter> parameters, List<Instruction> instructions) {
        boolean flag = false;

        List<String> nameParameters = parameters.stream()
                                                .map(Parameter::getName)
                                                .collect(Collectors.toList());
        List<String> leftOperands = instructions.stream()
                                                .map(Instruction::getLeftOperand)
                                                .distinct()
                                                .collect(Collectors.toList());

        if (nameParameters.containsAll(leftOperands)) {
            flag = true;
        }

        return flag;
    }

    public boolean checkOperators(List<Instruction> instructions) {
        boolean flag = false;

        List<String> operators = instructions.stream()
                                            .map(Instruction::getOperator)
                                            .distinct()
                                            .collect(Collectors.toList());
        if (Arrays.asList(LIST_OPERATORS).containsAll(operators)) {
            flag = true;
        }

        return flag;
    }

    private boolean checkNumber(String line) {
        try {
            double number = Double.parseDouble(line);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }

        return true;
    }

    public boolean checkExpression(List<Parameter> parameters, List<Instruction> instructions) throws ContractException {
        double leftOperand  = 0;
        double rightOperand = 0;

        Class type = double.class;
        boolean flag = false;

        if (parameters.stream()
                      .map(Parameter::getType)
                      .noneMatch(Class::isPrimitive)) {
            throw new ContractException("Types parameters must be primitive");
        }

        for (Instruction instruction : instructions) {
            if (!checkNumber(instruction.getLeftOperand()) && checkNumber(instruction.getRightOperand())) {
                type = takeType(parameters, instruction.getLeftOperand());

                leftOperand  = takeOperand(type, parameters, instruction.getLeftOperand());
                rightOperand = takeOperand(type, instruction.getRightOperand());
            } else if (checkNumber(instruction.getLeftOperand()) && !checkNumber(instruction.getRightOperand())) {
                type = takeType(parameters, instruction.getRightOperand());

                leftOperand  = takeOperand(type, instruction.getLeftOperand());
                rightOperand = takeOperand(type, parameters, instruction.getRightOperand());
            } else if (!checkNumber(instruction.getLeftOperand()) && !checkNumber(instruction.getRightOperand())) {
                type = takeType(parameters, instruction.getLeftOperand());

                leftOperand  = takeOperand(type, parameters, instruction.getLeftOperand());
                rightOperand = takeOperand(type, parameters, instruction.getRightOperand());
            } else {
                leftOperand  = takeOperand(instruction.getLeftOperand());
                rightOperand = takeOperand(instruction.getLeftOperand());
            }

            if (!checkContract(leftOperand, instruction.getOperator(), rightOperand)) {
                flag = false;
                break;
            } else {
                flag = true;
            }
        }

        return flag;
    }

    private boolean checkContract(double leftOperand, String operator, double rightOperand) {
        boolean flag = false;

        switch (operator) {
            case ">"  : flag = leftOperand >  rightOperand;  break;
            case ">=" : flag = leftOperand >= rightOperand;  break;
            case "<"  : flag = leftOperand <  rightOperand;  break;
            case "<=" : flag = leftOperand <= rightOperand;  break;
            case "!=" : flag = leftOperand != rightOperand;  break;
            case "==" : flag = leftOperand == rightOperand;  break;
        }

        return flag;
    }

    private double takeOperand(String operand) {
        return Double.valueOf(operand);
    }

    private double takeOperand(Class type, String operand) {
        double result = 0;

        if (type == byte.class) {
            result = Byte.valueOf(operand);
        } else if (type == short.class) {
            result = Short.valueOf(operand);
        } else if (type == int.class) {
            result = Integer.valueOf(operand);
        } else if (type == long.class) {
            result = Long.valueOf(operand);
        } else if (type == float.class) {
            result = Float.valueOf(operand);
        } else if (type == double.class) {
            result = Double.valueOf(operand);
        }

        return result;
    }

    private double takeOperand(Class type, List<Parameter> parameters, String operand) {
        double result = 0;

        Object value = parameters.stream()
                                 .filter(x -> x.getName().equals(operand))
                                 .map(Parameter::getValue)
                                 .collect(toSingleton());
        if (type == byte.class) {
            result = (byte) value;
        } else if (type == short.class) {
            result = (short) value;
        } else if (type == int.class) {
            result = (int) value;
        } else if (type == long.class) {
            result = (long) value;
        } else if (type == float.class) {
            result = (float) value;
        } else if (type == double.class) {
            result = (double) value;
        }

        return result;
    }

    private Class takeType(List<Parameter> parameters, String operand) {
        return parameters.stream()
                .filter(x -> x.getName().equals(operand))
                .map(Parameter::getType)
                .collect(toSingleton());
    }


    private boolean isCorrectValue(String value) {
        return value.matches(".*\\d+.*") || value.equals("null") ||
                value.equals("true")     || value.equals("false");
    }

    private static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }

                    return list.get(0);
                }
        );
    }

    public String getListOperators() {
        return Arrays.toString(LIST_OPERATORS);
    }
}
