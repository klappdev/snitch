package org.kl.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.kl.bean.Instruction;
import org.kl.bean.Variable;
import org.kl.contract.Invariant;
import org.kl.error.ContractException;
import org.kl.handle.ContractHandler;

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
            List<Instruction> instructions = ContractHandler.getInstance().parseLine(line);

            if (!ContractHandler.getInstance().checkOperators(instructions)) {
                throw new ContractException("Operator is not correct. Support operators: " +
                        ContractHandler.getInstance().getListOperators());
            }

            if (!ContractHandler.getInstance().checkExpression(fields, instructions)) {
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
