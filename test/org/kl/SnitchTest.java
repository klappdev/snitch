package org.kl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kl.bean.Instruction;
import org.kl.bean.Person;
import org.kl.bean.Variable;
import org.kl.bean.Value;
import org.kl.error.ContractException;
import org.kl.handle.ContractHandler;
import org.kl.state.Gender;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SnitchTest {
    private static List<Instruction> instructions;
    private static List<Variable> variables;
    private Instruction instruction;
    private String line;

    @BeforeAll
    public static void initialize() {
        variables = new ArrayList<>();
        instructions = new ArrayList<>();
    }

    @AfterAll
    public static void finalizes() {
        variables.clear();
        instructions.clear();
    }

    @Disabled
    @Test
    public void parseLineTest() throws ContractException {
        /* 1 */
        line = "x > 0";

        instruction = ContractHandler.getInstance().parseLine(line).get(0);

        assertEquals(instruction, new Instruction("x", ">",  "0"));

        /* 2 */
        line = "x > 1 || y >= 5";

        instructions = ContractHandler.getInstance().parseLine(line);

        assertEquals(instructions.get(0), new Instruction("x", ">",  "1"));
        assertEquals(instructions.get(1), new Instruction("y", ">=", "5"));

        /* 3 */
        line = "x > 1 || y >= x && y < 50";

        instructions = ContractHandler.getInstance().parseLine(line);

        assertEquals(instructions.get(0), new Instruction("x", ">",  "1"));
        assertEquals(instructions.get(1), new Instruction("y", ">=", "x"));
        assertEquals(instructions.get(2), new Instruction("y", "<", "50"));
    }

    @Test
    public void parseLineRoutineTest() throws ContractException {
        /* 1 */
        line = "x.contains()";

        instruction = ContractHandler.getInstance().parseLineRoutine(line).get(0);

        assertEquals(instruction, new Instruction("x", ".",  "contains"));
    }

    @Test
    public void checkExpectsTest() throws ContractException {
        /* 1 */
        int x = 9;
        double y = 9.0;

        variables.add(new Variable(int.class, "x", x));
        variables.add(new Variable(double.class, "y", y));
        instructions.add(new Instruction("x", ">", "0"));
        instructions.add(new Instruction("y", ">", "5"));

        assertTrue(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 2 */
        int a = 5;
        double b = 1.0;

        instructions.clear();
        variables.clear();
        variables.add(new Variable(int.class, "a", a));
        variables.add(new Variable(double.class, "b", b));
        instructions.add(new Instruction("a", ">", "-1"));
        instructions.add(new Instruction("b", "<", "2.0"));

        assertTrue(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 3 */
        int c = 5;
        int d = 1;

        instructions.clear();
        variables.clear();
        variables.add(new Variable(int.class, "c", c));
        variables.add(new Variable(int.class, "d", d));
        instructions.add(new Instruction("c", ">", "d"));

        assertTrue(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 4 */
        short e = 10;
        short f = 15;

        instructions.clear();
        variables.clear();
        variables.add(new Variable(short.class, "e", e));
        variables.add(new Variable(short.class, "f", f));
        instructions.add(new Instruction("e", ">", "5"));
        instructions.add(new Instruction("e", "<", "f"));

        assertTrue(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 5 */
        boolean j = false;

        instructions.clear();
        variables.clear();
        variables.add(new Variable(boolean.class, "j", j));
        instructions.add(new Instruction("true", "!=", "j"));

        assertTrue(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 6 */
        instructions.clear();
        variables.clear();
        variables.add(new Variable(boolean.class, "j", j));
        instructions.add(new Instruction("j", "==", "false"));

        assertTrue(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 7 */
        Person p = null;

        instructions.clear();
        variables.clear();
        variables.add(new Variable(Person.class, "p", p));
        instructions.add(new Instruction("p", "==", "null"));

        assertTrue(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 8 */
        instructions.clear();
        variables.clear();
        variables.add(new Variable(Person.class, "p", p));
        instructions.add(new Instruction("null", "!=", "p"));

        assertFalse(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 9 */
        Gender g = Gender.MALE;

        instructions.clear();
        variables.clear();
        variables.add(new Variable(Gender.class, "g", g));
        instructions.add(new Instruction("g", "==", "org.kl.state.Gender.MALE"));

        assertTrue(ContractHandler.getInstance().checkExpression(variables, instructions));

        /* 10 */
        instructions.clear();
        variables.clear();
        variables.add(new Variable(Gender.class, "g", g));
        instructions.add(new Instruction("org.kl.state.Gender.FEMALE", "==", "g"));

        assertFalse(ContractHandler.getInstance().checkExpression(variables, instructions));
    }

    @Disabled
    @Test
    public void checkEnsuresTest() throws ContractException {
        /* 1 */
        Value value = new Value(int.class, 10);

        instructions.add(new Instruction("result", ">", "5"));

        assertTrue(ContractHandler.getInstance().checkExpression(value, variables, instructions));

        /* 2 */
        value = new Value(int.class, 15);

        instructions.clear();
        variables.clear();
        instructions.add(new Instruction("10", "<=", "result"));

        assertTrue(ContractHandler.getInstance().checkExpression(value, variables, instructions));

        /* 3 */
        value = new Value(double.class, 15.0);

        instructions.clear();
        variables.clear();
        instructions.add(new Instruction("15.0", "!=", "result"));

        assertFalse(ContractHandler.getInstance().checkExpression(value, variables, instructions));

        /* 4 */
        short x = 5;
        short data = 10;
        value = new Value(short.class, data);

        instructions.clear();
        variables.clear();
        variables.add(new Variable(short.class, "x", x));
        instructions.add(new Instruction("result", ">", "x"));

        assertTrue(ContractHandler.getInstance().checkExpression(value, variables, instructions));

        /* 5 */
        long y = 15;
        long datum = 10;
        value = new Value(long.class, datum);

        instructions.clear();
        variables.clear();
        variables.add(new Variable(long.class, "y", y));
        instructions.add(new Instruction("y", "!=", "result"));

        assertTrue(ContractHandler.getInstance().checkExpression(value, variables, instructions));

        /* 6 */
        value = new Value(boolean.class, true);

        instructions.clear();
        variables.clear();
        instructions.add(new Instruction("true", "!=", "result"));

        assertTrue(ContractHandler.getInstance().checkExpression(value, variables, instructions));

        /* 7 */
        value = new Value(boolean.class, true);

        instructions.clear();
        variables.clear();
        instructions.add(new Instruction("result", "==", "false"));

        assertFalse(ContractHandler.getInstance().checkExpression(value, variables, instructions));

        /* 8 */
        value = new Value(Integer.class, 1);

        instructions.clear();
        variables.clear();
        instructions.add(new Instruction("result", "==", "null"));

        assertFalse(ContractHandler.getInstance().checkExpression(value, variables, instructions));

        /* 9 */
        value = new Value(Double.class, 5);

        instructions.clear();
        variables.clear();
        instructions.add(new Instruction("null", "!=", "result"));

        assertTrue(ContractHandler.getInstance().checkExpression(value, variables, instructions));
    }
}
