package io.kaif.web.api;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.zone.Zone;
import io.kaif.test.MvcIntegrationTests;

public class ArticleResourceTest extends MvcIntegrationTests {

  private Account account = accountTourist("foo");
  private String accessToken;

  @Before
  public void setUp() throws Exception {
    accessToken = prepareAccessToken(account);
  }

  @Test
  public void createExternalLink() throws Exception {
    String json = "{"
        + "  'zone':'programming',"
        + "  'title':'valid article',"
        + "  'url':'https://kekeke.cc/中文網址'"
        + "}";
    mockMvc.perform(put("/api/article/external-link").header(AccountAccessToken.HEADER_KEY,
        accessToken).contentType(MediaType.APPLICATION_JSON).content(q(json)))
        .andExpect(status().isOk());

    verify(articleService).createExternalLink(Mockito.isA(AccountAccessToken.class),
        eq(Zone.valueOf("programming")),
        eq("valid article"),
        eq("https://kekeke.cc/中文網址"));
  }

  @Test
  public void createExternalLink_validate_url() throws Exception {
    String json = "{"
        + "  'zone':'programming',"
        + "  'title':'bad url',"
        + "  'url':'http:/'"
        + "}";
    mockMvc.perform(put("/api/article/external-link").header(AccountAccessToken.HEADER_KEY,
        accessToken).contentType(MediaType.APPLICATION_JSON).content(q(json)))
        .andExpect(status().isBadRequest());

    verifyZeroInteractions(articleService);
  }

  @Test
  public void urlValidation() throws Exception {
    Pattern pattern = Pattern.compile(ArticleResource.CreateExternalLink.URL_PATTERN);

    // part of samples list in
    //   https://mathiasbynens.be/demo/url-regex
    String valid = ""
        + "http://foo.com/blah_blah\n"
        + "http://foo.com/blah_blah/\n"
        + "http://foo.com/blah_blah_(wikipedia)\n"
        + "http://foo.com/blah_blah_(wikipedia)_(again)\n"
        + "http://www.example.com/wpstyle/?p=364\n"
        + "https://www.example.com/foo/?bar=baz&inga=42&quux\n"
        + "http://142.42.1.1/\n"
        + "http://142.42.1.1:8080/\n"
        + "http://foo.com/blah_(wikipedia)#cite-1\n"
        + "http://foo.com/blah_(wikipedia)_blah#cite-1\n"
        + "http://foo.com/unicode_(✪)_in_parens\n"
        + "http://foo.com/(something)?after=parens\n"
        + "http://code.google.com/events/#&product=browser\n"
        + "http://j.mp\n"
        + "ftp://foo.bar/baz\n"
        + "http://foo.bar/?q=Test%20URL-encoded%20stuff\n"
        + "http://مثال.إختبار\n"
        + "http://例子.测试\n"
        + "http://1337.net\n"
        + "http://a.b-c.de\n"
        + "https://foo_bar.com/baz\n"
        + "https://foo-bar.com/baz\n"
        + "http://223.255.255.254";
    Pattern.compile("\n").splitAsStream(valid).forEach(url -> {
      assertTrue(url + " should be valid url", pattern.matcher(url).matches());
    });

    // `notSupport` are possible valid urls but we do not support yet.
    String notSupport = ""
        + "http://-.~_!$&'()*+,;=:%40:80%2f::::::@example.com\n"
        + "http://उदाहरण.परीक्षा\n"
        + "http://☺.damowmow.com/\n"
        + "http://✪df.ws/123\n"
        + "http://⌘.ws\n"
        + "http://⌘.ws/\n"
        + "http://➡.ws/䨹\n"
        + "http://userid:password@example.com:8080\n"
        + "http://userid:password@example.com:8080/\n"
        + "http://userid@example.com\n"
        + "http://userid@example.com/\n"
        + "http://userid@example.com:8080\n"
        + "http://userid@example.com:8080/\n"
        + "http://userid:password@example.com\n"
        + "http://userid:password@example.com/";

    Pattern.compile("\n").splitAsStream(notSupport).forEach(url -> {
      assertFalse("'" + url + "' not treat as valid url", pattern.matcher(url).matches());
    });

    String invalid = ""
        + "http://\n"
        + "http://.\n"
        + "http://..\n"
        + "http://../\n"
        + "http://?\n"
        + "http://??\n"
        + "http://??/\n"
        + "http://#\n"
        + "http://##\n"
        + "http://##/\n"
        + "//\n"
        + "//a\n"
        + "///a\n"
        + "///\n"
        + "http:///a\n"
        + "http://3628126748\n"
        + "foo.com\n"
        + "rdar://1234\n"
        + "h://test\n"
        + "http:// shouldfail.com\n"
        + "http://.www.foo.bar/\n"
        + "http://.www.foo.bar./\n"
        + ":// should fail";
    Pattern.compile("\n").splitAsStream(invalid).forEach(url -> {
      assertFalse("'" + url + "' should be invalid url", pattern.matcher(url).matches());
    });

    // these are not valid urls, but we ignore them to prevent rules become too complex
    String ignoreInvalid = ""
        + "http://0.0.0.0\n"
        + "http://-error-.invalid/\n"
        + "http://a.b--c.de/\n"
        + "http://foo.bar/foo(bar)baz quux\n"
        + "http://foo.bar?q=Spaces should be encoded\n"
        + "http://-a.b.co\n"
        + "http://a.b-.co\n"
        + "http://10.1.1.0\n"
        + "http://10.1.1.255\n"
        + "http://224.1.1.1\n"
        + "http://1.1.1.1.1\n"
        + "http://123.123.123\n"
        + "http://www.foo.bar./\n"
        + "http://10.1.1.1";
    Pattern.compile("\n").splitAsStream(ignoreInvalid).forEach(url -> {
      assertTrue("'" + url + "' is ignored", pattern.matcher(url).matches());
    });
  }
}