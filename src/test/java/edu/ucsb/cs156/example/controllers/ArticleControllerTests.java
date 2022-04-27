package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticleRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ArticleController.class)
@Import(TestConfig.class)
public class ArticleControllerTests extends ControllerTestCase {

        @MockBean
        ArticleRepository articleRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/Article/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/Article/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/Article/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/Article?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/Article/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Article/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Article/post"))
                                .andExpect(status().is(403));
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-04-26T00:00:00");

                Article article = Article.builder()
                                .title("Test Article 1")
                                .url("/api/Article?id=1")
                                .explanation("Checks if logged in user can get by id when the id exists")
                                .email("cwdougher@ucsb.edu")
                                .dateAdded(ldt)
                                .build();

                when(articleRepository.findById(eq(1L))).thenReturn(Optional.of(article));

                // act
                MvcResult response = mockMvc.perform(get("/api/Article?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(articleRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(article);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(articleRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/Article?id=1"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(articleRepository, times(1)).findById(eq(1L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Article with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_articles() throws Exception {

                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-04-26T00:00:00");

                Article article1 = Article.builder()
                                .title("Test Article 1")
                                .url("/api/Article?id=0")
                                .explanation("Checks if logged in user can get by id when the id exists")
                                .email("cwdougher@ucsb.edu")
                                .dateAdded(ldt)
                                .build();

                Article article2 = Article.builder()
                                .title("Test Article 2")
                                .url("/api/Article?id=1")
                                .explanation("Checks if logged in user can get all articles")
                                .email("cwdougher@ucsb.edu")
                                .dateAdded(ldt)
                                .build();

                ArrayList<Article> expectedarticle = new ArrayList<>();
                expectedarticle.addAll(Arrays.asList(article1, article2));

                when(articleRepository.findAll()).thenReturn(expectedarticle);

                // act
                MvcResult response = mockMvc.perform(get("/api/Article/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(articleRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedarticle);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_article() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-04-26T00:00:00");

                Article article = Article.builder()
                                .title("Test Article 1")
                                .url("/api/Article?id=0")
                                .explanation("Checks if admin user can post a new article")
                                .email("cwdougher@ucsb.edu")
                                .dateAdded(ldt)
                                .build();

                when(articleRepository.save(eq(article))).thenReturn(article);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/Article/post?title=Test Article 1&url=/api/Article?id=0&explanation=Checks if admin user can post a new article&email=cwdougher@ucsb.edu&dateAdded=2022-04-26T00:00:00")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(articleRepository, times(1)).save(article);
                String expectedJson = mapper.writeValueAsString(article);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
}