package org.kl.bean;

import java.util.Objects;

public class Instruction {
    private String operator;
    private String leftOperand;
    private String rightOperand;

    public Instruction(String leftOperand, String operator, String rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(String leftOperand) {
        this.leftOperand = leftOperand;
    }

    public String getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(String rightOperand) {
        this.rightOperand = rightOperand;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Instruction instruction = (Instruction) other;

        return  Objects.equals(operator, instruction.operator) &&
                Objects.equals(leftOperand, instruction.leftOperand) &&
                Objects.equals(rightOperand, instruction.rightOperand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, leftOperand, rightOperand);
    }

    @Override
    public String toString() {
        return "Instruction:\n" +
                "\toperator:    " + operator    +  "\n" +
                "\tleftOperand:        " + leftOperand +  "\n" +
                "\trightOperand:       " + rightOperand;
    }
}
