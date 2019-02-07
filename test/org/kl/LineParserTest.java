package org.kl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kl.bean.Instruction;
import org.kl.bean.Parameter;
import org.kl.bean.Value;
import org.kl.error.ContractException;
import org.kl.parse.LineParser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineParserTest {
    private static List<Instruction> instructions;
    private static List<Parameter> parameters;
    private Instruction instruction;
    private Parameter parameter;
    private String line;

    @BeforeAll
    public static void initialize() {
        parameters   = new ArrayList<>();
        instructions = new ArrayList<>();
    }

    @AfterAll
    public static void finalizes() {
        parameters.clear();
        instructions.clear();
    }

    @Disabled
    @Test
    public void parseLineTest() throws ContractException {
        /* 1 */
        line = "x > 0";

        instruction = LineParser.getInstance().parseLine(line).get(0);

        assertEquals(instruction, new Instruction("x", ">",  "0"));

        /* 2 */
        line = "x > 1 || y >= 5";

        instructions = LineParser.getInstance().parseLine(line);

        assertEquals(instructions.get(0), new Instruction("x", ">",  "1"));
        assertEquals(instructions.get(1), new Instruction("y", ">=", "5"));

        /* 3 */
        line = "x > 1 || y >= x && y < 50";

        instructions = LineParser.getInstance().parseLine(line);

        assertEquals(instructions.get(0), new Instruction("x", ">",  "1"));
        assertEquals(instructions.get(1), new Instruction("y", ">=", "x"));
        assertEquals(instructions.get(2), new Instruction("y", "<", "50"));
    }

    @Disabled
    @Test
    public void checkExpectsTest() throws ContractException {
        /* 1 */
        int x = 9;
        double y = 9.0;

        parameters.add(new Parameter(int.class, "x", x));
        parameters.add(new Parameter(double.class, "y", y));
        instructions.add(new Instruction("x", ">", "0"));
        instructions.add(new Instruction("y", ">", "5"));

        assertTrue(LineParser.getInstance().checkExpression(parameters, instructions));

        /* 2 */
        int a = 5;
        double b = 1.0;

        instructions.clear();
        parameters.clear();
        parameters.add(new Parameter(int.class, "a", a));
        parameters.add(new Parameter(double.class, "b", b));
        instructions.add(new Instruction("a", ">", "-1"));
        instructions.add(new Instruction("b", "<", "2.0"));

        assertTrue(LineParser.getInstance().checkExpression(parameters, instructions));

        /* 3 */
        int c = 5;
        int d = 1;

        instructions.clear();
        parameters.clear();
        parameters.add(new Parameter(int.class, "c", c));
        parameters.add(new Parameter(int.class, "d", d));
        instructions.add(new Instruction("c", ">", "d"));

        assertTrue(LineParser.getInstance().checkExpression(parameters, instructions));

        /* 4 */
        short e = 10;
        short f = 15;

        instructions.clear();
        parameters.clear();
        parameters.add(new Parameter(short.class, "e", e));
        parameters.add(new Parameter(short.class, "f", f));
        instructions.add(new Instruction("e", ">", "5"));
        instructions.add(new Instruction("e", "<", "f"));

        assertTrue(LineParser.getInstance().checkExpression(parameters, instructions));
    }

    @Test
    public void checkEnsuresTest() throws ContractException {
        /* 1 */
        Value value = new Value(int.class, 10);

        instructions.add(new Instruction("result", ">", "5"));

        assertTrue(LineParser.getInstance().checkExpression(value, parameters, instructions));

        /* 2 */
        value = new Value(int.class, 15);

        instructions.clear();
        parameters.clear();
        instructions.add(new Instruction("10", "<=", "result"));

        assertTrue(LineParser.getInstance().checkExpression(value, parameters, instructions));

        /* 3 */
        value = new Value(double.class, 15.0);

        instructions.clear();
        parameters.clear();
        instructions.add(new Instruction("15.0", "!=", "result"));

        assertFalse(LineParser.getInstance().checkExpression(value, parameters, instructions));

        /* 4 */
        short x = 5;
        short data = 10;
        value = new Value(short.class, data);

        instructions.clear();
        parameters.clear();
        parameters.add(new Parameter(short.class, "x", x));
        instructions.add(new Instruction("result", ">", "x"));

        assertTrue(LineParser.getInstance().checkExpression(value, parameters, instructions));

        /* 5 */
        long y = 15;
        long datum = 10;
        value = new Value(long.class, datum);

        instructions.clear();
        parameters.clear();
        parameters.add(new Parameter(long.class, "y", y));
        instructions.add(new Instruction("y", "!=", "result"));

        assertTrue(LineParser.getInstance().checkExpression(value, parameters, instructions));

        /* 6 */
        value = new Value(boolean.class, true);

        instructions.clear();
        parameters.clear();
        instructions.add(new Instruction("true", "!=", "result"));

        assertTrue(LineParser.getInstance().checkExpression(value, parameters, instructions));

        /* 7 */
        value = new Value(boolean.class, true);

        instructions.clear();
        parameters.clear();
        instructions.add(new Instruction("result", "==", "false"));

        assertFalse(LineParser.getInstance().checkExpression(value, parameters, instructions));

        /* 8 */
        value = new Value(Integer.class, 1);

        instructions.clear();
        parameters.clear();
        instructions.add(new Instruction("result", "==", "null"));

        assertFalse(LineParser.getInstance().checkExpression(value, parameters, instructions));

        /* 9 */
        value = new Value(Double.class, 5);

        instructions.clear();
        parameters.clear();
        instructions.add(new Instruction("null", "!=", "result"));

        assertTrue(LineParser.getInstance().checkExpression(value, parameters, instructions));
    }
}
