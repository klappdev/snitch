package org.kl.bean;


import java.util.Objects;

public class Parameter {
    private Class type;
    private String name;
    private Object value;

    public Parameter(Class type, String name, Object value) {
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

        Parameter parameter = (Parameter) other;

        return  Objects.equals(type, parameter.type) &&
                Objects.equals(name, parameter.name) &&
                Objects.equals(value, parameter.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, value);
    }

    @Override
    public String toString() {
        return "Parameter:\n" +
                "\ttype:    " + type  +  "\n" +
                "\tname:    " + name  +  "\n" +
                "\tvalue:   " + value;
    }
}
