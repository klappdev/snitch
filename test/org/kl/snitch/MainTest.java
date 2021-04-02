/*
 * Licensed under the MIT License <http://opensource.org/licenses/MIT>.
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2019 - 2021 https://github.com/klappdev
 *
 * Permission is hereby  granted, free of charge, to any  person obtaining a copy
 * of this software and associated  documentation files (the "Software"), to deal
 * in the Software  without restriction, including without  limitation the rights
 * to  use, copy,  modify, merge,  publish, distribute,  sublicense, and/or  sell
 * copies  of  the Software,  and  to  permit persons  to  whom  the Software  is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE  IS PROVIDED "AS  IS", WITHOUT WARRANTY  OF ANY KIND,  EXPRESS OR
 * IMPLIED,  INCLUDING BUT  NOT  LIMITED TO  THE  WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR  A PARTICULAR PURPOSE AND  NONINFRINGEMENT. IN NO EVENT  SHALL THE
 * AUTHORS  OR COPYRIGHT  HOLDERS  BE  LIABLE FOR  ANY  CLAIM,  DAMAGES OR  OTHER
 * LIABILITY, WHETHER IN AN ACTION OF  CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE  OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kl.snitch;

import org.kl.snitch.contract.Ensures;
import org.kl.snitch.contract.Expects;
import org.kl.snitch.contract.Invariant;

@Invariant("x != 0 && y != 0")
public final class MainTest {
    private int x;
    private int y;

    public MainTest() {
        this.x = 5;
        this.y = 10;
    }

    @Expects("line != null")
    @Ensures("result != false")
    private boolean isNull(String line) {
        return line.isEmpty();
    }

    @Expects("x > -1 && x > y")
    private boolean compare(float x, float y) {
        return x > y;
    }

    public static void main(String[] args) {
        MainTest mainTest = new MainTest();

        int x = 20;
        int y = 15;

        System.out.println("compare : " + mainTest.compare(x, y));
        System.out.println("isEmpty : " + mainTest.isNull("String"));
    }
}
