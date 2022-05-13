package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

        @MockBean
        UserRepository userRepository;


        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem?id=123"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post"))
                                .andExpect(status().is(403)); // only admins can post
        }


        @WithMockUser(roles = { "USER" })
        @Test 
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {


                UCSBDiningCommonsMenuItem menuItem = UCSBDiningCommonsMenuItem.builder()
                                .name("ortega")
                                .diningCommonsCode("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(123L))).thenReturn(Optional.of(menuItem));

                MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem?id=123"))
                                .andExpect(status().isOk()).andReturn();


                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(123L));
                String expectedJson = mapper.writeValueAsString(menuItem);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {


                when(ucsbDiningCommonsMenuItemRepository.findById(eq(123L))).thenReturn(Optional.empty());

                MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem?id=123"))
                                .andExpect(status().isNotFound()).andReturn();


                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(123L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenuItem with id 123 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test 
        public void logged_in_user_can_get_all_ucsbdiningcommons() throws Exception {

                UCSBDiningCommonsMenuItem menuItem1 = UCSBDiningCommonsMenuItem.builder()
                                .name("ortega")
                                .diningCommonsCode("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")
                                .build();

                UCSBDiningCommonsMenuItem menuItem2 = UCSBDiningCommonsMenuItem.builder()
                                .name("ortega")
                                .diningCommonsCode("Tofu Banh Mi Sandwich (v)")
                                .station("Entree Specials")
                                .build();

                ArrayList<UCSBDiningCommonsMenuItem> expectedMenuItems = new ArrayList<>();
                expectedMenuItems.addAll(Arrays.asList(menuItem1, menuItem2));

                when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedMenuItems);

                // act
                MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMenuItems);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_commons() throws Exception {

                UCSBDiningCommonsMenuItem menuItem = UCSBDiningCommonsMenuItem.builder()
                                .name("ortega")
                                .diningCommonsCode("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.save(eq(menuItem))).thenReturn(menuItem);

                MvcResult response = mockMvc.perform(
                                post("/api/UCSBDiningCommonsMenuItem/post?name=ortega&diningCommonsCode=Baked Pesto Pasta with Chicken&station=Entree Specials")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(menuItem);
                String expectedJson = mapper.writeValueAsString(menuItem);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem menuItem = UCSBDiningCommonsMenuItem.builder()
                                .name("ortega")
                                .diningCommonsCode("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(123L))).thenReturn(Optional.of(menuItem));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/UCSBDiningCommonsMenuItem?id=123")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(123L);
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 123 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(123L))).thenReturn(Optional.empty());

                MvcResult response = mockMvc.perform(
                                delete("/api/UCSBDiningCommonsMenuItem?id=123")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 123 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {

                UCSBDiningCommonsMenuItem menuItemOrig = UCSBDiningCommonsMenuItem.builder()
                                .name("ortega")
                                .diningCommonsCode("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")
                                .build();

                UCSBDiningCommonsMenuItem menuItemEdited = UCSBDiningCommonsMenuItem.builder()
                                .name("ortega")
                                .diningCommonsCode("Tofu Banh Mi Sandwich (v)")
                                .station("Entree Specials")
                                .build();

                String requestBody = mapper.writeValueAsString(menuItemEdited);

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(123L))).thenReturn(Optional.of(menuItemOrig));

                MvcResult response = mockMvc.perform(
                                put("/api/UCSBDiningCommonsMenuItem?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(123L);
                verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(menuItemEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {

                UCSBDiningCommonsMenuItem editedMenuItem = UCSBDiningCommonsMenuItem.builder()
                                .name("ortega")
                                .diningCommonsCode("Baked Pesto Pasta with Chicken")
                                .station("Entree Specials")
                                .build();

                String requestBody = mapper.writeValueAsString(editedMenuItem);

                when(ucsbDiningCommonsMenuItemRepository.findById(eq(123L))).thenReturn(Optional.empty());

                MvcResult response = mockMvc.perform(
                                put("/api/UCSBDiningCommonsMenuItem?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 123 not found", json.get("message"));

        }
}