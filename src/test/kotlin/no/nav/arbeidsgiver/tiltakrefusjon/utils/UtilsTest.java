package no.nav.arbeidsgiver.tiltakrefusjon.utils;

import org.junit.jupiter.api.Test;

import static no.nav.arbeidsgiver.tiltakrefusjon.utils.UtilsKt.erIkkeTomme;
import static no.nav.arbeidsgiver.tiltakrefusjon.utils.UtilsKt.erNoenTomme;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {
  @Test
  public void erIkkeTomme__med_null() {
    assertFalse(erIkkeTomme(1, "k", null));
  }

  @Test
  public void erIkkeTomme__med_tom_streng() {
    assertFalse(erIkkeTomme(1, "k", ""));
  }

  @Test
  public void erIkkeTomme__uten_null() {
    assertTrue(erIkkeTomme(1, "k", new Object()));
  }

  @Test
  public void erNoenTomme_med_gyldig_objekter() {
    assertFalse(erNoenTomme(1, "k", new Object()));
  }

  @Test
  public void erNoenTomme_med_delvis_tomme_objekter() {
    assertTrue(erNoenTomme(1, "k", new Object(), ""));
  }
}