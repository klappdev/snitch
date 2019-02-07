package org.kl.parse;

import org.kl.bean.Instruction;
import org.kl.bean.Parameter;
import org.kl.bean.Value;
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

    private boolean checkParameter(List<Parameter> parameters, String operand) {
        return parameters.stream()
                         .anyMatch(x -> x.getName().equals(operand));
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

    private boolean checkFlag(String operand) {
        return (operand.equals("true")) || (operand.equals("false"));
    }

    private boolean checkNull(String operand) {
        return operand.equals("null");
    }

    private boolean checkResult(String line) {
        return line.equalsIgnoreCase("result");
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
            String leftValue  = instruction.getLeftOperand();
            String rightValue = instruction.getRightOperand();

            if (checkParameter(parameters, leftValue) && checkNumber(rightValue)) {
                /* x <=> 0 */
                type = takeType(parameters, leftValue);

                leftOperand  = takeOperand(type, parameters, leftValue);
                rightOperand = takeOperand(type, rightValue);
            } else if (checkNumber(leftValue) && checkParameter(parameters, rightValue)) {
                /* 0 <=> x */
                type = takeType(parameters, rightValue);

                leftOperand  = takeOperand(type, leftValue);
                rightOperand = takeOperand(type, parameters, rightValue);
            } else if (checkParameter(parameters, leftValue) && checkParameter(parameters, rightValue)) {
                /* x <=> y */
                type = takeType(parameters, leftValue);

                leftOperand  = takeOperand(type, parameters, leftValue);
                rightOperand = takeOperand(type, parameters, rightValue);
            } else if (checkParameter(parameters, leftValue) && checkFlag(rightValue)) {
                /* x <=> true vs false */
                type = takeType(parameters, leftValue);
                checkConditionFlag(type, instruction.getOperator());

                leftOperand  = takeOperand(type, parameters, leftValue);
                rightOperand = rightValue.equals("true") ? 1 : 0;
            } else if (checkFlag(leftValue) && checkParameter(parameters, rightValue)) {
                /* true vs false <=> x */
                type = takeType(parameters, leftValue);
                checkConditionFlag(type, instruction.getOperator());

                leftOperand  = leftValue.equals("true") ? 1 : 0;
                rightOperand = takeOperand(type, parameters, rightValue);
            } else {
                /* 0 <=> 0 */
                throw new ContractException("Compare two number has not any effect");
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

    public boolean checkExpression(Value value, List<Parameter> parameters, List<Instruction> instructions) throws ContractException {
        double leftOperand  = 0;
        double rightOperand = 0;

        boolean flag   = false;
        Class type = double.class;

        for (Instruction instruction : instructions) {
            String leftValue  = instruction.getLeftOperand();
            String rightValue = instruction.getRightOperand();

            if (checkResult(leftValue) && checkNumber(rightValue)) {
                /* result <=> 0 */
                checkConditionResult(leftValue);

                leftOperand  = takeResult(value);
                rightOperand = takeOperand(value.getType(), rightValue);
            } else if (checkNumber(leftValue) && checkResult(rightValue)) {
                /* 0 <=> result */
                checkConditionResult(rightValue);

                leftOperand  = takeOperand(value.getType(), leftValue);
                rightOperand = takeResult(value);
            } else if (checkResult(leftValue) && checkParameter(parameters, rightValue)) {
                /* x <=> result */
                checkConditionResult(leftValue);

                type = takeType(parameters, rightValue);

                leftOperand  = takeResult(value);
                rightOperand = takeOperand(type, parameters, rightValue);
            } else if (checkParameter(parameters, leftValue) && checkResult(rightValue)) {
                /* result <=> x */
                checkConditionResult(rightValue);

                type = takeType(parameters, leftValue);

                leftOperand  = takeOperand(type, parameters, leftValue);
                rightOperand = takeResult(value);
            } else if (checkResult(leftValue) && checkFlag(rightValue)) {
                /* result <=> true vs false */
                checkConditionFlag(value.getType(), instruction.getOperator(), leftValue);

                leftOperand  = String.valueOf(value.getData()).equals("true") ? 1 : 0;
                rightOperand = rightValue.equals("true") ? 1 : 0;
            } else if (checkFlag(leftValue) && checkResult(rightValue)) {
                /* true vs false <=> result */
                checkConditionFlag(value.getType(), instruction.getOperator(), rightValue);

                leftOperand = leftValue.equals("true") ? 1 : 0;
                rightOperand = value.getData().equals("true") ? 1 : 0;
            } else if (checkResult(leftValue) && checkNull(rightValue)) {
                /* result <=> null */
                checkConditionObject(value, instruction.getOperator(), leftValue);

                leftOperand  = value.getData() != null ? 1 : 0;
                rightOperand = 0;
            } else if (checkNull(leftValue) && checkResult(rightValue)) {
                /* null <=> right */
                checkConditionObject(value, instruction.getOperator(), rightValue);

                leftOperand  = 0;
                rightOperand = value.getData() != null ? 1 : 0;
            } else {
                throw new ContractException("Correct return instruction: result operator right \n" +
                                            " or left operator result");
            }

            if (!checkContract(leftOperand, instruction.getOperator(), rightOperand)) {
                flag = false; break;
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

    private double takeOperand(Class type, List<Parameter> parameters, String operand) throws ContractException {
        double result = 0;
        Object value;

        try {
            value = parameters.stream()
                              .filter(x -> x.getName().equals(operand))
                              .map(Parameter::getValue)
                              .collect(toSingleton());
        } catch (IllegalStateException e) {
            throw new ContractException("Operand is not number");
        }

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
        } else if (type == boolean.class) {
            result = (boolean) value ? 1 : 0;
        }

        return result;
    }

    private double takeResult(Value value) {
        double result = 0;

        if (value.getType() == byte.class) {
            result = (byte) value.getData();
        } else if (value.getType() == short.class) {
            result = (short) value.getData();
        } else if (value.getType() == int.class) {
            result = (int) value.getData();
        } else if (value.getType() == long.class) {
            result = (long) value.getData();
        } else if (value.getType() == float.class) {
            result = (float) value.getData();
        } else if (value.getType() == double.class) {
            result = (double) value.getData();
        }

        return result;
    }

    private Class takeType(List<Parameter> parameters, String operand) {
        return parameters.stream()
                .filter(x -> x.getName().equals(operand))
                .map(Parameter::getType)
                .collect(toSingleton());
    }

    private void checkConditionObject(Value value, String operator, String operand) throws ContractException {
        if (!operand.equalsIgnoreCase("result")) {
            throw new ContractException("Name return variable must be - result");
        }

        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for flags: != or ==");
        }

        if (value == null) {
            throw new ContractException("Type return value must be - object");
        }
    }

    private void checkConditionFlag(Class type, String operator, String operand) throws ContractException {
        if (!operand.equalsIgnoreCase("result")) {
            throw new ContractException("Name return variable must be - result");
        }

        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for flags: != or ==");
        }

        if (type != boolean.class) {
            throw new ContractException("Type return value must be - boolean");
        }
    }

    private void checkConditionFlag(Class type, String operator) throws ContractException {
        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for flags: != or ==");
        }

        if (type != boolean.class) {
            throw new ContractException("Type return value must be - boolean");
        }
    }

    private void checkConditionResult(String operand) throws ContractException {
        if (!operand.equalsIgnoreCase("result")) {
            throw new ContractException("Name return variable must be - result");
        }
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
