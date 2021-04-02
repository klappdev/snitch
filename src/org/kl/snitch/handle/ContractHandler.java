/*
 * Licensed under the MIT License <http://opensource.org/licenses/MIT>.
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2019 - 2021 https://github.com/klappdev
 *
 * Permission is hereby  granted, free of charge, to any  person obtaining a copy
 * of this software and associated  documentation files (the "Software"), to deal
 * in the Software  without restriction, including without  limitation the rights
 * to  use, copy,  modify, merge,  publish, distribute,  sublicense, and/or  sell
 * copies  of  the Software,  and  to  permit persons  to  whom  the Software  is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE  IS PROVIDED "AS  IS", WITHOUT WARRANTY  OF ANY KIND,  EXPRESS OR
 * IMPLIED,  INCLUDING BUT  NOT  LIMITED TO  THE  WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR  A PARTICULAR PURPOSE AND  NONINFRINGEMENT. IN NO EVENT  SHALL THE
 * AUTHORS  OR COPYRIGHT  HOLDERS  BE  LIABLE FOR  ANY  CLAIM,  DAMAGES OR  OTHER
 * LIABILITY, WHETHER IN AN ACTION OF  CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE  OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kl.snitch.handle;

import org.kl.snitch.util.Instruction;
import org.kl.snitch.util.Variable;
import org.kl.snitch.util.Value;
import org.kl.snitch.error.ContractException;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class ContractHandler {
    private ContractHandler() throws ContractException {
        throw new ContractException("Can't create object");
    }

    public static boolean handleExpression(List<Variable> variables, List<Instruction> instructions) throws ContractException {
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

            if (ContractVerifier.checkParameter(variables, leftValue) && ContractVerifier.checkNumber(rightValue)) {
                /* x <=> 0 */
                type = takeType(variables, leftValue);

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = takeOperand(type, rightValue);
            } else if (ContractVerifier.checkNumber(leftValue) && ContractVerifier.checkParameter(variables, rightValue)) {
                /* 0 <=> x */
                type = takeType(variables, rightValue);

                leftOperand  = takeOperand(type, leftValue);
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (ContractVerifier.checkParameter(variables, leftValue) && ContractVerifier.checkParameter(variables, rightValue)) {
                /* x <=> y */
                type = takeType(variables, leftValue);

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (ContractVerifier.checkParameter(variables, leftValue) && ContractVerifier.checkFlag(rightValue)) {
                /* x <=> true vs false */
                type = takeType(variables, leftValue);
                ContractVerifier.testFlag(type, instruction.getOperator());

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = rightValue.equals("true") ? 1 : 0;
            } else if (ContractVerifier.checkFlag(leftValue) && ContractVerifier.checkParameter(variables, rightValue)) {
                /* true vs false <=> x */
                type = takeType(variables, rightValue);
                ContractVerifier.testFlag(type, instruction.getOperator());

                leftOperand  = leftValue.equals("true") ? 1 : 0;
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (ContractVerifier.checkParameter(variables, leftValue) && ContractVerifier.checkNull(rightValue)) {
                /* x <=> null */
                type = takeType(variables, leftValue);
                ContractVerifier.testInstance(type, instruction.getOperator());

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = 0;
            } else if (ContractVerifier.checkNull(leftValue) && ContractVerifier.checkParameter(variables, rightValue)) {
                /* null <=> x */
                type = takeType(variables, rightValue);
                ContractVerifier.testInstance(type, instruction.getOperator());

                leftOperand  = 0;
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (ContractVerifier.checkParameter(variables, leftValue) && ContractVerifier.checkEnumerator(rightValue)) {
                /* x <=> enum */
                type = takeType(variables, leftValue);
                ContractVerifier.testEnum(type, instruction.getOperator());

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = takeOperand(type, rightValue);
            } else if (ContractVerifier.checkEnumerator(leftValue) && ContractVerifier.checkParameter(variables, rightValue)) {
                /* enum <=> x */
                type = takeType(variables, rightValue);
                ContractVerifier.testEnum(type, instruction.getOperator());

                leftOperand = takeOperand(type, leftValue);
                rightOperand  = takeOperand(type, variables, rightValue);
            } else {
                /* 0 <=> 0 */
                throw new ContractException("Compare two parameters has not any effect");
            }

            if (!processExpression(leftOperand, instruction.getOperator(), rightOperand)) {
                flag = false;
                break;
            } else {
                flag = true;
            }
        }

        return flag;
    }

    public static boolean handleExpression(Value value, List<Variable> variables, List<Instruction> instructions) throws ContractException {
        double leftOperand  = 0;
        double rightOperand = 0;

        boolean flag   = false;
        Class type = double.class;

        for (Instruction instruction : instructions) {
            String leftValue  = instruction.getLeftOperand();
            String rightValue = instruction.getRightOperand();

            if (ContractVerifier.checkResult(leftValue) && ContractVerifier.checkNumber(rightValue)) {
                /* result <=> 0 */
                ContractVerifier.testResult(leftValue);

                leftOperand  = takeResult(value);
                rightOperand = takeOperand(value.getType(), rightValue);
            } else if (ContractVerifier.checkNumber(leftValue) && ContractVerifier.checkResult(rightValue)) {
                /* 0 <=> result */
                ContractVerifier.testResult(rightValue);

                leftOperand  = takeOperand(value.getType(), leftValue);
                rightOperand = takeResult(value);
            } else if (ContractVerifier.checkResult(leftValue) && ContractVerifier.checkParameter(variables, rightValue)) {
                /* result <=> x */
                ContractVerifier.testResult(leftValue);

                type = takeType(variables, rightValue);

                leftOperand  = takeResult(value);
                rightOperand = takeOperand(type, variables, rightValue);
            } else if (ContractVerifier.checkParameter(variables, leftValue) && ContractVerifier.checkResult(rightValue)) {
                /* x <=> result */
                ContractVerifier.testResult(rightValue);

                type = takeType(variables, leftValue);

                leftOperand  = takeOperand(type, variables, leftValue);
                rightOperand = takeResult(value);
            } else if (ContractVerifier.checkResult(leftValue) && ContractVerifier.checkFlag(rightValue)) {
                /* result <=> true vs false */
                ContractVerifier.testFlag(value, instruction.getOperator(), leftValue);

                leftOperand  = String.valueOf(value.getData()).equals("true") ? 1 : 0;
                rightOperand = rightValue.equals("true") ? 1 : 0;
            } else if (ContractVerifier.checkFlag(leftValue) && ContractVerifier.checkResult(rightValue)) {
                /* true vs false <=> result */
                ContractVerifier.testFlag(value, instruction.getOperator(), rightValue);

                leftOperand  = leftValue.equals("true") ? 1 : 0;
                rightOperand = value.getData().equals("true") ? 1 : 0;
            } else if (ContractVerifier.checkResult(leftValue) && ContractVerifier.checkNull(rightValue)) {
                /* result <=> null */
                ContractVerifier.testInstance(value, instruction.getOperator(), leftValue);

                leftOperand  = value.getData() != null ? 1 : 0;
                rightOperand = 0;
            } else if (ContractVerifier.checkNull(leftValue) && ContractVerifier.checkResult(rightValue)) {
                /* null <=> right */
                ContractVerifier.testInstance(value, instruction.getOperator(), rightValue);

                leftOperand  = 0;
                rightOperand = value.getData() != null ? 1 : 0;
            } else if (ContractVerifier.checkResult(leftValue) && ContractVerifier.checkEnumerator(rightValue)) {
                /* result <=> enum */
                ContractVerifier.testEnum(value, instruction.getOperator(), leftValue);

                leftOperand  = takeResult(value);
                rightOperand = takeOperand(value.getType(), rightValue);
            } else if (ContractVerifier.checkEnumerator(leftValue) && ContractVerifier.checkResult(rightValue)) {
                /* enum <=> result */
                ContractVerifier.testEnum(value, instruction.getOperator(), rightValue);

                leftOperand  = takeOperand(value.getType(), leftValue);
                rightOperand = takeResult(value);
            } else {
                throw new ContractException("Correct return instruction: result operator right \n" +
                                            " or left operator result");
            }

            if (!processExpression(leftOperand, instruction.getOperator(), rightOperand)) {
                flag = false; break;
            } else {
                flag = true;
            }
        }

        return flag;
    }

    private static boolean processExpression(double leftOperand, String operator, double rightOperand) {
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

    @SuppressWarnings("unchecked")
    private static double takeOperand(Class type, String operand) {
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

    private static double takeOperand(Class type, List<Variable> variables, String operand) throws ContractException {
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
    private static double takeResult(Value value) {
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

    private static Class takeType(List<Variable> variables, String operand) {
        return variables.stream()
                        .filter(x -> x.getName().equals(operand))
                        .map(Variable::getType)
                        .collect(toSingleton());
    }

    private static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1)
                        throw new IllegalStateException();
                    return list.get(0);
                }
        );
    }
}
