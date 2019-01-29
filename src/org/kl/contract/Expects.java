/**
 * @author kl
 * @version 1.0
 *
 * Annotation help developers add precondition
 * to method classes for check parameters
 *
 * Support next operations:
 * 1) check one level condition
 * 2) support only comparison operations
 * 3) support only numbers and null
 */
package org.kl.contract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expects {
    String value();
}
