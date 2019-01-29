package org.kl.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.kl.bean.Instruction;
import org.kl.bean.Parameter;
import org.kl.contract.Expects;
import org.kl.error.ContractException;
import org.kl.parse.LineParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Aspect
public class ExpectsAspect {

    @Before("execution(* *(..)) && @annotation(expects)")
    public void precondition(JoinPoint point, Expects expects) throws ContractException {
        List<Parameter> parameters = new ArrayList<>();
        List<Instruction> instructions = new ArrayList<>();

        CodeSignature codeSignature = (CodeSignature) point.getSignature();
        Class[]  types = codeSignature.getParameterTypes();
        String[] names = codeSignature.getParameterNames();
        Object[] args  = point.getArgs();

        for (int i = 0; i < args.length; i++) {
            parameters.add(new Parameter(types[i], names[i], args[i]));
        }

        String   line  = expects.value();
        String[] parts = LineParser.getInstance().parseLine(line);

        System.out.println("value: " + line);

        if (parameters.size() == 1) {
            if (parts.length == 3) {
                String operator = parts[1];
                String name     = parts[0];
                String value    = parts[2];

                instructions.add(new Instruction(operator, name, value));

                if (instructions.stream().noneMatch(x -> x.getName().equals(name))) {
                    throw new ContractException("Parameter name is not correct");
                }

                if (!LineParser.getInstance().isSupportOperations(operator)) {
                    throw new ContractException("Operation is not support");
                }

                if (!LineParser.getInstance().isCorrectValue(value)) {
                    throw new ContractException("Value is not correct");
                }

                if (!LineParser.getInstance().checkArguments(parameters.get(0), instructions.get(0))) {
                    throw new ContractException("Contract is violated: " + line + ", where " + parameters.get(0).getName() + "" +
                                                " is " + parameters.get(0).getValue());
                }

                Arrays.stream(parts).forEach(System.out::println);
            } else {
                throw new ContractException("Condition is not correct");
            }
        } else {
            throw new ContractException("Method must has only one parameter");
        }
    }
}
