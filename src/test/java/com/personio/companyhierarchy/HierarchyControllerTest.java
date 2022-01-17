package com.personio.companyhierarchy;

import com.personio.companyhierarchy.service.HierarchyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class HierarchyControllerTest {

    public static String PERSONIO_API = "/personio";

    @Autowired
    MockMvc mvc;
    @MockBean
    HierarchyService service;

    @Test
    @DisplayName("Must return a valid company hierarchy")
    public void getCompanyHierarchy(){

    }

}
