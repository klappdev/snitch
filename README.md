# Snitch

This is simple Java contract library based on AspectJ.

Library can make compare operations with primitive type and 
check objects for null.<br/>
There are three type annotation which use for checking.

    @Expects   - check parameters method
    @Ensures   - check return value method
    @Invariant - check fields class


Developer can check input parameters method.

![alt text](https://raw.githubusercontent.com/klappdev/snitch/master/res/Expects.png)

Developer can check return value method. For return variable use reserved word result.

![alt text](https://raw.githubusercontent.com/klappdev/snitch/master/res/Ensures.png)

Developer also can check value fields class.

![alt text](https://raw.githubusercontent.com/klappdev/snitch/master/res/Invariant.png)

In future expects add check call method objects and IDE highlight syntax plugins.

Requirements:<br/>
JDK: Java 8 <br/>
libs: AspectJ
