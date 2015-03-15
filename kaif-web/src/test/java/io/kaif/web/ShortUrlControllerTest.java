package io.kaif.web;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;
import io.kaif.test.MvcIntegrationTests;

public class ShortUrlControllerTest extends MvcIntegrationTests {

  @Test
  public void redirectArticle() throws Exception {
    FlakeId flakeId = FlakeId.fromString("foobar123");
    Article article = article(Zone.valueOf("sysop"), flakeId, "my article title");
    when(articleService.findArticle(flakeId)).thenReturn(Optional.of(article));
    mockMvc.perform(get("/d/foobar123"))
        .andExpect(redirectedUrl("/z/sysop/debates/foobar123"))
        .andExpect(status().isPermanentRedirect());
  }

  @Test
  public void redirectDebate() throws Exception {
    FlakeId flakeId = FlakeId.fromString("foobar123");
    Article article = article(Zone.valueOf("programming"), flakeId, "my article title");
    Debate debate = debate(article, "my reply", null);

    when(articleService.findArticle(flakeId)).thenReturn(Optional.empty());
    when(articleService.loadDebateWithCache(flakeId)).thenReturn(debate);

    mockMvc.perform(get("/d/foobar123"))
        .andExpect(redirectedUrl("/z/programming/debates/foobar123/" + debate.getDebateId()))
        .andExpect(status().isPermanentRedirect());
  }

  @Test
  public void notFound() throws Exception {
    FlakeId flakeId = FlakeId.fromString("notExist");

    when(articleService.findArticle(flakeId)).thenReturn(Optional.empty());
    when(articleService.loadDebateWithCache(flakeId)).thenThrow(new EmptyResultDataAccessException(
        "",
        1));

    mockMvc.perform(get("/d/notExist")).andExpect(status().isNotFound());
  }
}