package de.neue_phase.asterisk.ClickDial.tests;

/**
 * Created by mky on 05.04.2015.
 */
public class EnumMappingTest {

    private enum TestMap {
        BLAH,
        YOUNG,
        TEST,
        UHH
    }

    public static void main(String[] args) {

        TestMap t = TestMap.valueOf ("YOUNG");
        System.out.print ("Mapped value is: " + t);

        t = TestMap.valueOf ("YOUNG1");
        System.out.print ("Mapped value is: " + t);
    }
}
