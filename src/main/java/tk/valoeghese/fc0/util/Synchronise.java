package tk.valoeghese.fc0.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker to suggest synchronisation when accessing, as it is accessed on multiple threads.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Synchronise {
}
