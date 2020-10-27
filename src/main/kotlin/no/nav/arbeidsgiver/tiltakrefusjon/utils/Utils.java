package no.nav.arbeidsgiver.tiltakrefusjon.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static boolean erIkkeTomme(Object... objekter) {
        for (Object objekt : objekter) {
            if (objekt instanceof String && ((String) objekt).isEmpty()) {
                return false;
            }
            if (objekt == null) {
                return false;
            }
        }
        return true;
    }
    public static boolean erNoenTomme(Object... objekter) {
        return !erIkkeTomme(objekter);
    }
}