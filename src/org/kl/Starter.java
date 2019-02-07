package org.kl;

import org.kl.contract.Ensures;
import org.kl.contract.Expects;
import org.kl.contract.Invariant;

@Invariant("x != 0 && y != 0")
public class Starter {
    private int x;
    private int y;

    public Starter() {
        this.x = 5;
        this.y = 10;
    }

    @Expects("x > 0")
    @Ensures("result < x")
    private double sqrt(int x) {
        return Math.sqrt(x);
    }

    @Expects("x > -1 && x > y")
    private boolean compare(float x, float y) {
        return x > y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static void main(String[] args) {
        Starter starter = new Starter();

        int x = 20;
        int y = 15;

        System.out.println("sqrt: " + starter.sqrt(x));
        System.out.println("compare : " + starter.compare(x, y));
    }
}
