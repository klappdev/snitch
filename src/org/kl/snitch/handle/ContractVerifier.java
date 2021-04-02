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
import org.kl.snitch.util.Value;
import org.kl.snitch.util.Variable;
import org.kl.snitch.error.ContractException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ContractVerifier {
    private static final String[] LIST_OPERATORS = {
            ">", ">=", "<", "<=", "!=", "=="
    };

    private ContractVerifier() throws ContractException {
        throw new ContractException("Can't create object");
    }

    /* package-private */ static void testInstance(Value value, String operator, String operand) throws ContractException {
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

    /* package-private */ static void testInstance(Class type, String operator) throws ContractException {
        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for object: != or ==");
        }

        if (type.isPrimitive()) {
            throw new ContractException("Type variable must be - object");
        }
    }

    /* package-private */ static void testEnum(Value value, String operator, String operand) throws ContractException {
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

    /* package-private */ static void testEnum(Class type, String operator) throws ContractException {
        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for enum: != or ==");
        }

        if (!type.isEnum()) {
            throw new ContractException("Type variable must be - enum");
        }
    }

    @SuppressWarnings("unchecked")
    /* package-private */ static void testFlag(Value value, String operator, String operand) throws ContractException {
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

    /* package-private */ static void testFlag(Class type, String operator) throws ContractException {
        if (!operator.equals("==") && !operator.equals("!=")) {
            throw new ContractException("Support operators for flags: != or ==");
        }

        if (type != boolean.class) {
            throw new ContractException("Type variable must be - boolean");
        }
    }

    /* package-private */ static void testResult(String operand) throws ContractException {
        if (!operand.equalsIgnoreCase("result")) {
            throw new ContractException("Name return variable must be - result");
        }
    }

    /* package-private */ static boolean checkParameter(List<Variable> variables, String operand) {
        return variables.stream()
                .anyMatch(x -> x.getName().equals(operand));
    }

    public static boolean checkOperators(List<Instruction> instructions) {
        List<String> operators = instructions.stream()
                                            .map(Instruction::getOperator)
                                            .distinct()
                                            .collect(Collectors.toList());
        return Arrays.asList(LIST_OPERATORS).containsAll(operators);
    }

    /* package-private */ static boolean checkNumber(String line) {
        try {
            Double.parseDouble(line);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }

        return true;
    }

    /* package-private */ static boolean checkEnumerator(String line) {
        try {
            String name = line.substring(0, line.lastIndexOf("."));

            return Class.forName(name).isEnum();
        } catch (StringIndexOutOfBoundsException | ClassNotFoundException e) {
            return false;
        }
    }

    /* package-private */ static boolean checkFlag(String operand) {
        return (operand.trim().equals("true")) || (operand.trim().equals("false"));
    }

    /* package-private */ static boolean checkNull(String operand) {
        return operand.trim().equals("null");
    }

    /* package-private */ static boolean checkResult(String line) {
        return line.equalsIgnoreCase("result");
    }

    public static String getListOperators() {
        return Arrays.toString(LIST_OPERATORS);
    }
}
