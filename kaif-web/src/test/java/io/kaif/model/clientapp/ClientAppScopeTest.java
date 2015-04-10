package io.kaif.model.clientapp;

import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Test;

public class ClientAppScopeTest {

  @Test
  public void tryParse() throws Exception {
    assertEquals(EnumSet.of(ClientAppScope.PUBLIC), ClientAppScope.tryParse("public"));
    assertEquals(EnumSet.of(ClientAppScope.PUBLIC, ClientAppScope.FEED),
        ClientAppScope.tryParse("feed public"));
    assertEquals(EnumSet.of(ClientAppScope.ARTICLE, ClientAppScope.VOTE),
        ClientAppScope.tryParse(" Article  vote "));

    assertTrue(ClientAppScope.tryParse(null).isEmpty());
    assertTrue(ClientAppScope.tryParse(" ").isEmpty());
    assertTrue(ClientAppScope.tryParse(" foo ").isEmpty());
    assertTrue(ClientAppScope.tryParse("feed foo bar").isEmpty());
  }

  @Test
  public void canonicalString() throws Exception {
    assertEquals("article feed user",
        ClientAppScope.toCanonicalString(EnumSet.of(ClientAppScope.ARTICLE,
            ClientAppScope.USER,
            ClientAppScope.FEED)));
    assertEquals("feed public",
        ClientAppScope.toCanonicalString(EnumSet.of(ClientAppScope.FEED, ClientAppScope.PUBLIC)));
    assertEquals("feed public",
        ClientAppScope.toCanonicalString(EnumSet.of(ClientAppScope.PUBLIC, ClientAppScope.FEED)));
  }
}