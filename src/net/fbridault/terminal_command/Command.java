package net.fbridault.terminal_command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by felix on 04/05/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name() default "";
    String[] parameters() default {};
}
