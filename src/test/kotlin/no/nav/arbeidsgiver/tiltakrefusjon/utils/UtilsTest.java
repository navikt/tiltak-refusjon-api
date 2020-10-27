package no.nav.arbeidsgiver.tiltakrefusjon.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UtilsTest {
  @Test
  public void erIkkeTomme__med_null() {
    assertThat(Utils.erIkkeTomme(1, "k", null)).isFalse();
  }

  @Test
  public void erIkkeTomme__med_tom_streng() {
    assertThat(Utils.erIkkeTomme(1, "k", "")).isFalse();
  }

  @Test
  public void erIkkeTomme__uten_null() {
    assertThat(Utils.erIkkeTomme(1, "k", new Object())).isTrue();
  }

  @Test
  public void erNoenTomme_med_gyldig_objekter(){
    assertThat(Utils.erNoenTomme(1, "k", new Object())).isFalse();
  }

  @Test
  public void erNoenTomme_med_delvis_tomme_objekter(){
    assertThat(Utils.erNoenTomme(1, "k", new Object(),"")).isTrue();
  }
}