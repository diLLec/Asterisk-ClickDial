package de.neue_phase.asterisk.ClickDial.constants;

import java.util.Arrays;

abstract class ConstantsBase {
    /**
     * returns a String[] of the enum names
     * @param e the enum
     * @return the result
     */
    public static String[] enumToStringArray (Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

}
