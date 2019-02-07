package org.kl.bean;

import java.util.Objects;

public class Value {
    private Class type;
    private Object data;

    public Value(Class type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Class getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Value value = (Value) other;

        return  Objects.equals(this.type, value.type) &&
                Objects.equals(this.data, value.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, data);
    }

    @Override
    public String toString() {
        return "Parameter:\n" +
                "\ttype:    " + type  +  "\n" +
                "\tdata:    " + data;
    }
}
