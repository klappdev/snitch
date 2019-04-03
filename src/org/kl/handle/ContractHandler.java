package org.kl.handle;

import org.kl.bean.Instruction;
import org.kl.bean.Variable;
import org.kl.bean.Value;
import org.kl.error.ContractException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ContractHandler {
    private final String[] LIST_OPERATORS = {
            ">", ">=",
            "<", "<=",
            "!=", "=="
    };

    private static ContractHandler instance;

    private ContractHandler() {}

    public static ContractHandler getInstance() {
        if (instance == null) {
            instance = new ContractHandler();
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

    public List<Instruction> parseLineRoutine(String line) throws ContractException {
        List<Instruction> list = new ArrayList<>();

        if (!line.endsWith("()")) {
            throw new ContractException("Call method object must end with ()");
        }

        if (!line.contains(".")) {
            throw new ContractException("After name object must follow dot");
        }

        String[] parts = line.split("\\.");

        if (parts.length != 2) {
            throw new ContractException("Correct call method: object.method()");
        }

        list.add(new Instruction(parts[0], ".", parts[1].substring(0, parts[1].length() - 2)));

        return list;
    }

    public boolean checkExpression(List<Variable> variables, List<Instruction> instructions) throws ContractException {
        double leftOperand  = 0;
        double rightOperand = 0;

        Class type = double.class;
        boolean flag = false;

        /*
        if (variables.stream()
                      .map(Variable::getType)
                      .noneMatch(Class::isPrimitive)) {
            throw new ContractException("Types variables must be primitive");
        }
        */

        for (Instruction instruction : instructions) {
            String leftValue  = instruction.getLeftOperand();
            String rightValue = instruction.getRightOperand();

            if (checkParameter(variables, leftValue) && checkNumber(rightValue)) {
                /* x <=> 0 */
                type = takeType(variables, leftValue);

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = takeOperand(type, rightValue);
            } else if (checkNumber(leftValue) && checkParameter(variables, rightValue)) {
                /* 0 <=> x */
                type = takeType(variables, rightValue);

                leftOperand  = takeOperand(type, leftValue);
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (checkParameter(variables, leftValue) && checkParameter(variables, rightValue)) {
                /* x <=> y */
                type = takeType(variables, leftValue);

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (checkParameter(variables, leftValue) && checkFlag(rightValue)) {
                /* x <=> true vs false */
                type = takeType(variables, leftValue);
                checkConditionFlag(type, instruction.getOperator());

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = rightValue.equals("true") ? 1 : 0;
            } else if (checkFlag(leftValue) && checkParameter(variables, rightValue)) {
                /* true vs false <=> x */
                type = takeType(variables, rightValue);
                checkConditionFlag(type, instruction.getOperator());

                leftOperand  = leftValue.equals("true") ? 1 : 0;
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (checkParameter(variables, leftValue) && checkNull(rightValue)) {
                /* x <=> null */
                type = takeType(variables, leftValue);
                checkConditionInstance(type, instruction.getOperator());

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = 0;
            } else if (checkNull(leftValue) && checkParameter(variables, rightValue)) {
                /* null <=> x */
                type = takeType(variables, rightValue);
                checkConditionInstance(type, instruction.getOperator());

                leftOperand  = 0;
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (checkParameter(variables, leftValue) && checkEnumerator(rightValue)) {
                /* x <=> enum */
                type = takeType(variables, leftValue);
                checkConditionEnum(type, instruction.getOperator());

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = takeOperand(type, rightValue);
            } else if (checkEnumerator(leftValue) && checkParameter(variables, rightValue)) {
                /* enum <=> x */
                type = takeType(variables, rightValue);
                checkConditionEnum(type, instruction.getOperator());

                leftOperand = takeOperand(type, leftValue);
                rightOperand  = takeOperand(type, variables, rightValue);
            } else {
                /* 0 <=> 0 */
                throw new ContractException("Compare two parameters has not any effect");
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

    public boolean checkExpression(Value value, List<Variable> variables, List<Instruction> instructions) throws ContractException {
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
            } else if (checkResult(leftValue) && checkParameter(variables, rightValue)) {
                /* result <=> x */
                checkConditionResult(leftValue);

                type = takeType(variables, rightValue);

                leftOperand  = takeResult(value);
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (checkParameter(variables, leftValue) && checkResult(rightValue)) {
                /* x <=> result */
                checkConditionResult(rightValue);

                type = takeType(variables, leftValue);

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = takeResult(value);
            } else if (checkResult(leftValue) && checkFlag(rightValue)) {
                /* result <=> true vs false */
                checkConditionFlag(value, instruction.getOperator(), leftValue);

                leftOperand  = String.valueOf(value.getData()).equals("true") ? 1 : 0;
                rightOperand = rightValue.equals("true") ? 1 : 0;
            } else if (checkFlag(leftValue) && checkResult(rightValue)) {
                /* true vs false <=> result */
                checkConditionFlag(value, instruction.getOperator(), rightValue);

                leftOperand  = leftValue.equals("true") ? 1 : 0;
                rightOperand = value.getData().equals("true") ? 1 : 0;
            } else if (checkResult(leftValue) && checkNull(rightValue)) {
                /* result <=> null */
                checkConditionInstance(value, instruction.getOperator(), leftValue);

                leftOperand  = value.getData() != null ? 1 : 0;
                rightOperand = 0;
            } else if (checkNull(leftValue) && checkResult(rightValue)) {
                /* null <=> right */
                checkConditionInstance(value, instruction.getOperator(), rightValue);

                leftOperand  = 0;
                rightOperand = value.getData() != null ? 1 : 0;
            } else if (checkResult(leftValue) && checkEnumerator(rightValue)) {
                /* result <=> enum */
                checkConditionEnum(value, instruction.getOperator(), leftValue);

                leftOperand  = takeResult(value);
                rightOperand = takeOperand(value.getType(), rightValue);
            } else if (checkEnumerator(leftValue) && checkResult(rightValue)) {
                /* enum <=> result */
                checkConditionEnum(value, instruction.getOperator(), rightValue);

                leftOperand  = takeOperand(value.getType(), leftValue);
                rightOperand = takeResult(value);
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

    private boolean checkParameter(List<Variable> variables, String operand) {
        return variables.stream()
                        .anyMatch(x -> x.getName().equals(operand));
    }

    public boolean checkOperators(List<Instruction> instructions) {
        List<String> operators = instructions.stream()
                .map(Instruction::getOperator)
                .distinct()
                .collect(Collectors.toList());
        return Arrays.asList(LIST_OPERATORS).containsAll(operators);
    }

    private boolean checkNumber(String line) {
        try {
            Double.parseDouble(line);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }

        return true;
    }

    private boolean checkEnumerator(String line) {
        try {
            String name = line.substring(0, line.lastIndexOf("."));

            return Class.forName(name).isEnum();
        } catch (StringIndexOutOfBoundsException | ClassNotFoundException e) {
            return false;
        }
    }

    private boolean checkFlag(String operand) {
        return (operand.trim().equals("true")) || (operand.trim().equals("false"));
    }

    private boolean checkNull(String operand) {
        return operand.trim().equals("null");
    }

    private boolean checkResult(String line) {
        return line.equalsIgnoreCase("result");
    }

    @SuppressWarnings("unchecked")
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
        } else if (type.isEnum()) {
            result = Enum.valueOf(type, operand.substring(operand.lastIndexOf(".") + 1)).ordinal();
        }

        return result;
    }

    private double takeOperand(Class type, List<Variable> variables, String operand) throws ContractException {
        double result = 0;
        Object value;

        try {
            value = variables.stream()
                              .filter(x -> x.getName().equals(operand))
                              .map(Variable::getValue)
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
        } else if (type == Object.class) {
            result = value != null ? 1 : 0;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
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
        } else if (value.getType().isEnum()) {
            result = Enum.valueOf(value.getType(), value.getData().toString()).ordinal();
        }

        return result;
    }

    private Class takeType(List<Variable> variables, String operand) {
        return variables.stream()
                        .filter(x -> x.getName().equals(operand))
                        .map(Variable::getType)
                        .collect(toSingleton());
    }

    private void checkConditionInstance(Value value, String operator, String operand) throws ContractException {
        if (!operand.equalsIgnoreCase("result")) {
            throw new ContractException("Name return variable must be - result");
        }

        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for object: != or ==");
        }

        if (value.getType().isPrimitive()) {
            throw new ContractException("Type return value must be - object");
        }
    }

    private void checkConditionInstance(Class type, String operator) throws ContractException {
        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for object: != or ==");
        }

        if (type.isPrimitive()) {
            throw new ContractException("Type variable must be - object");
        }
    }

    private void checkConditionEnum(Value value, String operator, String operand) throws ContractException {
        if (!operand.equalsIgnoreCase("result")) {
            throw new ContractException("Name return variable must be - result");
        }

        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for enum: != or ==");
        }

        if (!value.getType().isEnum()) {
            throw new ContractException("Type variable must be - enum");
        }
    }

    private void checkConditionEnum(Class type, String operator) throws ContractException {
        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for enum: != or ==");
        }

        if (!type.isEnum()) {
            throw new ContractException("Type variable must be - enum");
        }
    }

    @SuppressWarnings("unchecked")
    private void checkConditionFlag(Value value, String operator, String operand) throws ContractException {
        Class<Boolean> clazz = value.getType();

        if (!operand.equalsIgnoreCase("result")) {
            throw new ContractException("Name return variable must be - result");
        }

        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for flags: != or ==");
        }

        if (clazz != boolean.class) {
            throw new ContractException("Type return value must be - boolean");
        }
    }

    private void checkConditionFlag(Class type, String operator) throws ContractException {
        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for flags: != or ==");
        }

        if (type != boolean.class) {
            throw new ContractException("Type variable must be - boolean");
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
