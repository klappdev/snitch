package org.kl.reflect;

public class Reflection {

    public static boolean isPrimitiviable(Class<?> inClass, Class<?> outClass) {
        boolean flag = false;

        if (inClass.isPrimitive()) {
            if ((inClass == byte.class   && outClass == Byte.class)      ||
                (inClass == short.class  && outClass == Short.class)     ||
                (inClass == int.class    && outClass == Integer.class)   ||
                (inClass == long.class   && outClass == Long.class)      ||
                (inClass == float.class  && outClass == Float.class)     ||
                (inClass == double.class && outClass == Double.class)    ||
                (inClass == char.class   && outClass == Character.class) ||
                (inClass == boolean.class && outClass == Boolean.class)) {
                flag = true;
            }
        }

        return flag;
    }
}
