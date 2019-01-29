package org.kl.bean;

import java.util.Objects;

public class Instruction {
    private String operator;
    private String name;
    private String value;

    public Instruction(String operator, String name, String value) {
        this.operator = operator;
        this.name = name;
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
                Objects.equals(name, instruction.name) &&
                Objects.equals(value, instruction.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, name, value);
    }

    @Override
    public String toString() {
        return "Instruction:\n" +
                "\toperator:    " + operator    +  "\n" +
                "\tname:        " + name        +  "\n" +
                "\tvalue:       " + value;
    }
}
