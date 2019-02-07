package org.kl.bean;


import java.util.Objects;

public class Variable {
    private Class type;
    private String name;
    private Object value;

    public Variable(Class type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public Class getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Variable variable = (Variable) other;

        return  Objects.equals(type, variable.type) &&
                Objects.equals(name, variable.name) &&
                Objects.equals(value, variable.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, value);
    }

    @Override
    public String toString() {
        return "Variable:\n" +
                "\ttype:    " + type  +  "\n" +
                "\tname:    " + name  +  "\n" +
                "\tvalue:   " + value;
    }
}
