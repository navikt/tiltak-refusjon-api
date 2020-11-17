package no.nav.arbeidsgiver.tiltakrefusjon.utils;

import org.junit.Test;

import static no.nav.arbeidsgiver.tiltakrefusjon.utils.UtilsKt.erIkkeTomme;
import static no.nav.arbeidsgiver.tiltakrefusjon.utils.UtilsKt.erNoenTomme;
import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {
  @Test
  public void erIkkeTomme__med_null() {
    assertThat(erIkkeTomme(1, "k", null)).isFalse();
  }

  @Test
  public void erIkkeTomme__med_tom_streng() {
    assertThat(erIkkeTomme(1, "k", "")).isFalse();
  }

  @Test
  public void erIkkeTomme__uten_null() {
    assertThat(erIkkeTomme(1, "k", new Object())).isTrue();
  }

  @Test
  public void erNoenTomme_med_gyldig_objekter(){
    assertThat(erNoenTomme(1, "k", new Object())).isFalse();
  }

  @Test
  public void erNoenTomme_med_delvis_tomme_objekter(){
    assertThat(erNoenTomme(1, "k", new Object(),"")).isTrue();
  }
}