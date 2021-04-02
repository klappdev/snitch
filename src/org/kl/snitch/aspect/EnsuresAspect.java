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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.kl.snitch.util.Instruction;
import org.kl.snitch.util.Variable;
import org.kl.snitch.util.Value;
import org.kl.snitch.contract.Ensures;
import org.kl.snitch.error.ContractException;
import org.kl.snitch.handle.ContractHandler;
import org.kl.snitch.handle.ContractParser;
import org.kl.snitch.handle.ContractVerifier;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Aspect
public class EnsuresAspect {

    @AfterReturning(pointcut = "execution(* *(..)) && @annotation(ensures)", returning = "result")
    public void postcondition(JoinPoint point, Ensures ensures, Object result) throws ContractException {
        String line = ensures.value();
        Value value = initValue(point, result);

        try {
            List<Variable> variables = initParameters(point);
            List<Instruction> instructions = ContractParser.parseLine(line);

            if (!ContractVerifier.checkOperators(instructions)) {
                throw new ContractException("Operator is not correct. Support operators: " +
                          ContractVerifier.getListOperators());
            }

            if (!ContractHandler.handleExpression(value, variables, instructions)) {
                throw new ContractException("Contract is violated: " + line);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ContractException("Expression is not correct");
        }
    }

    @SuppressWarnings("Duplicates")
    private List<Variable> initParameters(JoinPoint point) {
        List<Variable> variables = new ArrayList<Variable>();

        CodeSignature codeSignature = (CodeSignature) point.getSignature();
        Class[]  types = codeSignature.getParameterTypes();
        String[] names = codeSignature.getParameterNames();
        Object[] args  = point.getArgs();

        for (int i = 0; i < args.length; i++) {
            variables.add(new Variable(types[i], names[i], args[i]));
        }

        return variables;
    }

    private Value initValue(JoinPoint point, Object result) {
        Value value = null;

        Method method = ((MethodSignature) point.getSignature()).getMethod();
        Type type = method.getGenericReturnType();

        if (type instanceof Class) {
            value = new Value((Class<?>) type, result);
        }

        return value;
    }
}
