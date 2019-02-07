package org.kl.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.kl.bean.Instruction;
import org.kl.bean.Parameter;
import org.kl.bean.Value;
import org.kl.contract.Ensures;
import org.kl.error.ContractException;
import org.kl.parse.LineParser;

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

        System.out.println("value: " + line);

        try {
            List<Parameter>   parameters   = initParameters(point);
            List<Instruction> instructions = LineParser.getInstance().parseLine(line);

            if (!LineParser.getInstance().checkOperators(instructions)) {
                throw new ContractException("Operator is not correct. Support operators: " +
                        LineParser.getInstance().getListOperators());
            }

            if (!LineParser.getInstance().checkExpression(value, parameters, instructions)) {
                throw new ContractException("Contract is violated: " + line);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ContractException("Expression is not correct");
        }
    }

    @SuppressWarnings("Duplicates")
    private List<Parameter> initParameters(JoinPoint point) {
        List<Parameter> parameters = new ArrayList<Parameter>();

        CodeSignature codeSignature = (CodeSignature) point.getSignature();
        Class[]  types = codeSignature.getParameterTypes();
        String[] names = codeSignature.getParameterNames();
        Object[] args  = point.getArgs();

        for (int i = 0; i < args.length; i++) {
            parameters.add(new Parameter(types[i], names[i], args[i]));
        }

        return parameters;
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
