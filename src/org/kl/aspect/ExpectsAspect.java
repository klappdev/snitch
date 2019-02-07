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
import java.util.List;

@Aspect
public class ExpectsAspect {

    @Before("execution(* *(..)) && @annotation(expects)")
    public void precondition(JoinPoint point, Expects expects) throws ContractException {
        String line  = expects.value();

        System.out.println("value: " + line);

        try {
            List<Parameter>   parameters   = initParameters(point);
            List<Instruction> instructions = LineParser.getInstance().parseLine(line);

            if (!LineParser.getInstance().checkOperators(instructions)) {
                throw new ContractException("Operator is not correct. Support operators: " +
                          LineParser.getInstance().getListOperators());
            }

            if (!LineParser.getInstance().checkExpression(parameters, instructions)) {
                throw new ContractException("Contract is violated: " + line +
                                            ", where " + parameters.get(0).getName() +
                                            " is " + parameters.get(0).getValue());
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
}
