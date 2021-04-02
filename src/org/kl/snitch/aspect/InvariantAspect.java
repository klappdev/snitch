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
package org.kl.snitch.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.kl.snitch.util.Instruction;
import org.kl.snitch.util.Variable;
import org.kl.snitch.contract.Invariant;
import org.kl.snitch.error.ContractException;
import org.kl.snitch.handle.ContractHandler;
import org.kl.snitch.handle.ContractParser;
import org.kl.snitch.handle.ContractVerifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Aspect
public class InvariantAspect {

    @Around("call((@Invariant *).new(..))")
    public Object invariant(ProceedingJoinPoint point) throws ContractException {
        Object value;
        String line;

        try {
            value = point.proceed();
            line  = value.getClass().getDeclaredAnnotation(Invariant.class).value();

            List<Variable> fields = initFields(value);
            List<Instruction> instructions = ContractParser.parseLine(line);

            if (!ContractVerifier.checkOperators(instructions)) {
                throw new ContractException("Operator is not correct. Support operators: " +
                          ContractVerifier.getListOperators());
            }

            if (!ContractHandler.handleExpression(fields, instructions)) {
                throw new ContractException("Contract is violated: " + line +
                                            ", where " + fields.get(0).getName());
            }
        } catch (ContractException e) {
            throw new ContractException("Expression is not correct " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ContractException("Can not get fields class " + e.getMessage());
        } catch (Throwable e) {
            throw new ContractException("Can not create object " + e.getMessage());
        }

        return value;
    }

    private List<Variable> initFields(Object value) throws IllegalAccessException {
        List<Variable> list = new ArrayList<Variable>();

        for (Field field : value.getClass().getDeclaredFields()) {
            if (!field.getName().startsWith("ajc")) {
                field.setAccessible(true);

                list.add(new Variable(field.getType(), field.getName(), field.get(value)));
            }
        }

        return list;
    }
}
