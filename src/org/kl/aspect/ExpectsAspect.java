package org.kl.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.kl.bean.Instruction;
import org.kl.bean.Variable;
import org.kl.contract.Expects;
import org.kl.error.ContractException;
import org.kl.handle.ContractHandler;

import java.util.ArrayList;
import java.util.List;

@Aspect
public class ExpectsAspect {

    @Before("execution(* *(..)) && @annotation(expects)")
    public void precondition(JoinPoint point, Expects expects) throws ContractException {
        String line  = expects.value();

        try {
            List<Variable> variables = initParameters(point);
            List<Instruction> instructions = ContractHandler.getInstance().parseLine(line);

            if (!ContractHandler.getInstance().checkOperators(instructions)) {
                throw new ContractException("Operator is not correct. Support operators: " +
                          ContractHandler.getInstance().getListOperators());
            }

            if (!ContractHandler.getInstance().checkExpression(variables, instructions)) {
                throw new ContractException("Contract is violated: " + line +
                                            ", where " + variables.get(0).getName());
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
}
