package com.acme.university;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.university.repository.LecturerRepository;
import com.acme.university.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
 * Integration tests for the student APIs (creation + assignment to a lecturer,
 * and retrieval).
 *
 * <p>Runs against an in-memory H2 database (see {@code application-test.yml}).
 * The stubbed tests below {@code fail()} on purpose — implement them.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentApiIntegrationTest {

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

    private void createLecturer(String id, String name, String surname) throws Exception {
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("lecturerId", id, "name", name, "surname", surname))))
                .andExpect(status().isCreated());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void addStudent_toExistingLecturer_createsAndAssigns() throws Exception {
        createLecturer("L1", "NameL1", "SurnameL1");

        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("studentId", "S1", "name", "NameS1", "surname", "SurnameS1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value("S1"))
                .andExpect(jsonPath("$.name").value("NameS1"))
                .andExpect(jsonPath("$.surname").value("SurnameS1"))
                .andExpect(jsonPath("$.lecturers", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.lecturers[0].lecturerId").value("L1"));

        assertThat(studentRepository.existsByStudentId("S1")).isTrue();
    }

    @Test
    void addStudent_whenStudentAlreadyExists_assignsExistingStudent() throws Exception {
        createLecturer("L1", "NameL1", "SurnameL1");
        createLecturer("L2", "NameL2", "SurnameL2");

        // First assignment creates the student.
        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("studentId", "S1", "name", "NameS1", "surname", "SurnameS1"))))
                .andExpect(status().isCreated());

        // Second assignment reuses the existing student.
        mockMvc.perform(post("/api/v1/lecturers/L2/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("studentId", "S1", "name", "NameS1", "surname", "SurnameS1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lecturers", Matchers.hasSize(2)));

        // No duplicate student row was created.
        assertThat(studentRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/students/S1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lecturers[*].lecturerId",
                        Matchers.containsInAnyOrder("L1", "L2")));
    }

    @Test
    void addStudent_toNonExistentLecturer_returnsError() throws Exception {
        mockMvc.perform(post("/api/v1/lecturers/UNKNOWN/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("studentId", "S1", "name", "NameS1", "surname", "SurnameS1"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    void addStudent_withInvalidName_returnsBadRequest() throws Exception {
        createLecturer("L1", "NameL1", "SurnameL1");

        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("studentId", "S1", "name", "", "surname", "SurnameS1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(" name ")));

        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("studentId", "S1", "name", "NameS1", "surname", "Sur name"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(" surname ")));

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    void getStudent_returnsStudentWithAssignedLecturers() throws Exception {
        createLecturer("L1", "NameL1", "SurnameL1");
        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("studentId", "S1", "name", "NameS1", "surname", "SurnameS1"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/students/S1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("S1"))
                .andExpect(jsonPath("$.name").value("NameS1"))
                .andExpect(jsonPath("$.surname").value("SurnameS1"))
                .andExpect(jsonPath("$.lecturers", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.lecturers[0].lecturerId").value("L1"));
    }

    @Test
    void getStudent_whenNotFound_returnsError() throws Exception {
        mockMvc.perform(get("/api/v1/students/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
