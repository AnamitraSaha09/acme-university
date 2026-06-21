package com.acme.university;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.university.repository.LecturerRepository;
import com.acme.university.repository.StudentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

/**
 * Integration tests for the lecturer APIs.
 *
 * <p>Runs against an in-memory H2 database (see {@code application-test.yml}).
 * The stubbed tests below {@code fail()} on purpose — implement them.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LecturerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void cleanDatabase() {
        // Deleting lecturer first since it is the owner of assignment
        // therefore on deleting students, won't face foreign key violation
        lecturerRepository.deleteAll();
        studentRepository.deleteAll();
    }


    @Test
    void createLecturer_returnsCreatedLecturer() throws Exception {
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("lecturerId", "L1", "name", "NameL1", "surname", "SurnameL1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lecturerId").value("L1"))
                .andExpect(jsonPath("$.name").value("NameL1"))
                .andExpect(jsonPath("$.surname").value("SurnameL1"))
                .andExpect(jsonPath("$.students").isArray())
                .andExpect(jsonPath("$.students").isEmpty());

        assertThat(lecturerRepository.existsByLecturerId("L1")).isTrue();
    }

    @Test
    void createLecturer_whenAlreadyExists_returnsError() throws Exception {
        String body = json(Map.of("lecturerId", "L1", "name", "NameL1", "surname", "SurnameL1"));

        mockMvc.perform(post("/api/v1/lecturers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/lecturers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertThat(lecturerRepository.count()).isEqualTo(1);
    }

    @Test
    void createLecturer_withInvalidName_returnsBadRequest() throws Exception {
        // Blank name.
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("lecturerId", "L1", "name", "  ", "surname", "SurnameL1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString(" name ")));

        // Non-alphanumeric surname.
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("lecturerId", "L1", "name", "NameL1", "surname", "Surname-L1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(" surname ")));

        assertThat(lecturerRepository.count()).isZero();
    }

    @Test
    void getLecturer_returnsLecturerWithAssignedStudents() throws Exception {
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("lecturerId", "L1", "name", "NameL1", "surname", "SurnameL1"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("studentId", "S1", "name", "NameS1", "surname", "SurnameS1"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/lecturers/L1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lecturerId").value("L1"))
                .andExpect(jsonPath("$.students", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.students[0].studentId").value("S1"))
                .andExpect(jsonPath("$.students[0].name").value("NameS1"))
                .andExpect(jsonPath("$.students[0].surname").value("SurnameS1"));
    }

    @Test
    void getLecturer_whenNotFound_returnsError() throws Exception {
        mockMvc.perform(get("/api/v1/lecturers/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String json(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
}
