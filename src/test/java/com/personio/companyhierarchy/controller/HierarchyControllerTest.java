package com.personio.companyhierarchy.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personio.companyhierarchy.exception.ErrorConstants;
import com.personio.companyhierarchy.service.HierarchyService;
import org.json.JSONException;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.hamcrest.Matchers;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class HierarchyControllerTest {

    public static String PERSONIO_API = "/personio/hierarchy";

    @Autowired
    MockMvc mvc;
    @MockBean
    HierarchyService service;


    @Test
    @DisplayName("Must create a valid company hierarchy")
    public void mustCreateCompanyHierarchySuccessfully() throws Exception {
        JSONObject json = new JSONObject();
        String body = "{\n" +
                "\t\"Barbara\": \"Nick\", \n" +
                "\t\"Nick\": \"Sophie\", \n" +
                "\t\"Sophie\": \"Jonas\"\n" +
                "}";
        JSONObject barbara = new JSONObject();
        JSONObject nick = new JSONObject();
        JSONObject sophie = new JSONObject();
        JSONObject jonas = new JSONObject();

        barbara.put("Barbara", JSONObject.NULL);
        nick.put("Nick",barbara);
        sophie.put("Sophie",nick);
        jonas.put("Jonas",sophie);

        BDDMockito.given(service.saveHierarchy(body)).willReturn(jonas);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(PERSONIO_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body);


        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("Jonas").isNotEmpty())
                .andExpect(jsonPath("Jonas.Sophie").isNotEmpty())
                .andExpect(jsonPath("Jonas.Sophie.Nick").isNotEmpty())
                .andExpect(jsonPath("Jonas.Sophie.Nick.Barbara").isEmpty())
                .andExpect(content().string("{\"Jonas\":{\"Sophie\":{\"Nick\":{\"Barbara\":null}}}}"));
    }

    @Test
    @DisplayName("Must fail due to creating a hierarchy with more than one boss")
    public void mustFailDueToMultipleBosses() throws Exception{
        String body = "{\n" +
                "\t\"Barbara\": \"Nick\", \n" +
                "\t\"Nick\": \"Sophie\", \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Luke\":\"Paul\"\n" +
                "}";

        BDDMockito.given(service.saveHierarchy(body)).willThrow(ErrorConstants.MORE_THAN_ONE_BOSS);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(PERSONIO_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body);
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("There is more than one boss in this hierarchy"));
    }

    @Test
    @DisplayName("Must fail due to creating a hierarchy with direct employees relation looping")
    public void mustFailDueToLoop() throws Exception{
        String body = "{\n" +
                "\t\"Barbara\": \"Nick\", \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\"\n" +
                "}";

        BDDMockito.given(service.saveHierarchy(body)).willThrow(ErrorConstants.LOOP_SUPERVISORS("Nick","Barbara"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(PERSONIO_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body);
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("There is a loop relation between employees 'Nick' and 'Barbara'"));
    }

    @Test
    @DisplayName("Must fail due to creating a hierarchy with employees relation looping")
    public void mustFailDueToLoopInEmployees() throws Exception{
        String body = "{\t \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\",\n" +
                "\t\"Barbara\":\"Jonas\",\n" +
                "\t\"Paul\":\"John\"\n" +
                "}";

        BDDMockito.given(service.saveHierarchy(body)).willThrow(ErrorConstants.LOOP_SUPERVISORS);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(PERSONIO_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body);
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("There is a loop in the hierarchy"));
    }

    @Test
    @DisplayName("Must fail due to one employee has more than one supervisor")
    public void mustFailDueToMultipleSupervisors() throws Exception{
        String body = "{\n" +
                "\t\"Barbara\": \"Nick\", \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\",\n" +
                "\t\"Barbara\":\"Jonas\"\n" +
                "}";

        BDDMockito.given(service.saveHierarchy(body)).willThrow(ErrorConstants.MULTIPLE_SUPERVISORS("Duplicate key \"Barbara\""));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(PERSONIO_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body);
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("The employee 'Barbara' must not have more than one supervisor."));
    }

    @Test
    @DisplayName("Must fail due to not exists a boss in the hierarchy")
    public void mustFailDueToNoBoss() throws Exception{
        String body = "{\t \n" +
                "\t\"Sophie\": \"Jonas\",\n" +
                "\t\"Nick\":\"Barbara\",\n" +
                "\t\"Barbara\":\"Jonas\",\n" +
                "\t\"Jonas\":\"Nick\"\n" +
                "}";

        BDDMockito.given(service.saveHierarchy(body)).willThrow(ErrorConstants.NO_BOSS);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(PERSONIO_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body);
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("There is no boss in this hierarchy"));
    }
}
