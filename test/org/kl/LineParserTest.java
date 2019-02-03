package org.kl;

import org.junit.jupiter.api.Test;
import org.kl.bean.Instruction;
import org.kl.parse.LineParser;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinePaserTest {

    @Test
    public void parseLineTest() {
        String line = "x > 0";

        Instruction instruction = LineParser.getInstance().parseLine(line).get(0);

        assertEquals("x", instruction.getLeftOperand());
        assertEquals("0", instruction.getRightOperand());
        assertEquals(">", instruction.getOperator());
    }
}
