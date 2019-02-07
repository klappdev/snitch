package org.kl;

import org.kl.contract.Ensures;
import org.kl.contract.Expects;

public class Starter {

    @Expects("x > 0")
    @Ensures("result < x")
    public double sqrt(int x) {
        return Math.sqrt(x);
    }

    @Expects("x > -1 && x > y")
    @Ensures("result == true")
    public boolean cmp(float x, float y) { return x > y; }

    public static void main(String[] args) {
        Starter starter = new Starter();

        int x = 20;
        int y = 15;

        System.out.println("sqrt: " + starter.sqrt(x));
        System.out.println("cmp : " + starter.cmp(x, y));
    }
}
