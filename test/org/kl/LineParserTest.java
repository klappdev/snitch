package org.kl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kl.bean.Instruction;
import org.kl.bean.Parameter;
import org.kl.error.ContractException;
import org.kl.parse.LineParser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineParserTest {
    private static List<Instruction> instructions;
    private static List<Parameter> parameters;
    private Instruction instruction;
    private Parameter parameter;
    private String line;

    @BeforeAll
    public static void initialize() {
        parameters = new ArrayList<>();
        instructions = new ArrayList<>();
    }

    @AfterAll
    public static void finalizes() {

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

    @Test
    public void checkExpressionTest() throws ContractException {
        /* 1 */
        line = "x > 0";

        Object x = 9.0;

        parameters.add(new Parameter(int.class, "x", x));
        instructions.add(new Instruction("x", ">", "0"));

        assertTrue(LineParser.getInstance().checkExpression(parameters, instructions));
    }
}
