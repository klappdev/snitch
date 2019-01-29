package org.kl;

import org.kl.contract.Expects;

public class Starter {

    @Expects("x > 0")
    public double sqrt(double x) {
        return Math.sqrt(x);
    }

    public static void main(String[] args) {
        Starter starter = new Starter();

        double x = -1;

        System.out.println("sqrt: " + starter.sqrt(x));
    }
}
