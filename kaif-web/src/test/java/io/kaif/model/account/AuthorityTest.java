package io.kaif.model.account;

import static io.kaif.model.account.Authority.CITIZEN;
import static io.kaif.model.account.Authority.FORBIDDEN;
import static io.kaif.model.account.Authority.SUFFRAGE;
import static io.kaif.model.account.Authority.SYSOP;
import static io.kaif.model.account.Authority.TOURIST;
import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Test;

public class AuthorityTest {

  @Test
  public void toBits() throws Exception {
    assertEquals(0L, Authority.toBits(EnumSet.noneOf(Authority.class)));
    assertEquals(1L, Authority.toBits(EnumSet.of(Authority.FORBIDDEN)));
    assertEquals(2L, Authority.toBits(EnumSet.of(TOURIST)));
    assertEquals(6L, Authority.toBits(EnumSet.of(TOURIST, CITIZEN)));
    assertEquals(14L, Authority.toBits(EnumSet.of(TOURIST, CITIZEN, SUFFRAGE)));
    assertEquals(30L, Authority.toBits(EnumSet.of(TOURIST, CITIZEN, SUFFRAGE, SYSOP)));
  }

  @Test
  public void fromBits() throws Exception {
    assertEquals(Authority.fromBits(0L), (EnumSet.noneOf(Authority.class)));
    assertEquals(Authority.fromBits(1L), (EnumSet.of(FORBIDDEN)));
    assertEquals(Authority.fromBits(2L), (EnumSet.of(TOURIST)));
    assertEquals(Authority.fromBits(6L), (EnumSet.of(TOURIST, CITIZEN)));
    assertEquals(Authority.fromBits(14L), (EnumSet.of(TOURIST, CITIZEN, SUFFRAGE)));
    assertEquals(Authority.fromBits(30L), (EnumSet.of(TOURIST, CITIZEN, SUFFRAGE, SYSOP)));
  }
}