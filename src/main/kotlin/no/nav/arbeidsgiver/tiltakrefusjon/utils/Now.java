package no.nav.arbeidsgiver.tiltakrefusjon.utils;

import java.time.*;

// Inspirasjon fra https://medium.com/agorapulse-stories/how-to-solve-now-problem-in-your-java-tests-7c7f4a6d703c
public class Now {

    private static ThreadLocal<Clock> clock = ThreadLocal.withInitial(Clock::systemDefaultZone);

    // Metode for å sette tidspunkt. Skal kun brukes i test
    public static void fixedDate(LocalDate localDate) {
        Instant instant = ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.systemDefault()).toInstant();
        clock.set(Clock.fixed(instant, ZoneId.systemDefault()));
    }

    // Metode for å sette dato og klokkeslett. Skal kun brukes i test
    public static void fixedDateTime(LocalDateTime localDateTime){
        Instant instant = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant();
        clock.set(Clock.fixed(instant, ZoneId.systemDefault()));
    }

    // Metode for å resette tidspunkt. Skal brukes i test hvis man har brukt fixedDate først
    public static void resetClock() {
        Now.clock.set(Clock.systemDefaultZone());
    }

    // Skal brukes i prod-kode om man trenger nå-tidspunkt. Ikke bruk Instant.now() i prod-kode om det skal kunne testes.
    public static Instant instant() {
        return Instant.now(clock.get());
    }

    // Skal brukes i prod-kode om man trenger nå-tidspunkt. Ikke bruk LocalDate.now() i prod-kode om det skal kunne testes.
    public static LocalDate localDate() {
        return LocalDate.now(clock.get());
    }

    // Skal brukes i prod-kode om man trenger nå-tidspunkt. Ikke bruk LocalDateTime.now() i prod-kode om det skal kunne testes.
    public static LocalDateTime localDateTime() {
        return LocalDateTime.now(clock.get());
    }

    // Skal brukes i prod-kode om man trenger nå-tidspunkt. Ikke bruk YearMonth.now() i prod-kode om det skal kunne testes.
    public static YearMonth yearMonth() {
        return YearMonth.now(clock.get());
    }
}
