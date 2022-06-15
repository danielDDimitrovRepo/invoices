package com.customer.invoices.util;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.fail;

public class CommonTestUtils {

    /**
     * Used to preserve encapsulation
     */
    public static <T> void setPrivateField(String fieldName, Object fieldValue, T obj) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            field.set(obj, fieldValue);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail("Cannot set private field:" + fieldName +
                            ", of class: " + obj.getClass() +
                            ", with fieldValue: " + fieldValue,
                    e);
        }
    }

}
