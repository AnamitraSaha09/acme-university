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
 * Runs against an in-memory H2 database
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

    private final String LECTURER_ID = "L1";
    private final String LECTURER_NAME = "NameL1";
    private final String LECTURER_SURNAME = "SurnameL1";
    private final String STUDENT_ID = "S1";
    private final String STUDENT_NAME = "NameS1";
    private final String STUDENT_SURNAME = "SurnameS1";

    private final String KEY_NAME = "name";
    private final String KEY_SURNAME = "surname";
    private final String KEY_LECTURER_ID = "lecturerId";
    private final String KEY_STUDENT_ID = "studentId";
    private final Map<String,String> LECTURER_CONTENT = Map.of(
            KEY_LECTURER_ID, LECTURER_ID,
            KEY_NAME, LECTURER_NAME,
            KEY_SURNAME, LECTURER_SURNAME
    );
    private final Map<String,String> STUDENT_CONTENT = Map.of(
            KEY_STUDENT_ID, STUDENT_ID,
            KEY_NAME, STUDENT_NAME,
            KEY_SURNAME, STUDENT_SURNAME
    );

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
                        .content(json(LECTURER_CONTENT)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lecturerId").value(LECTURER_ID))
                .andExpect(jsonPath("$.name").value(LECTURER_NAME))
                .andExpect(jsonPath("$.surname").value(LECTURER_SURNAME))
                .andExpect(jsonPath("$.students").isArray())
                .andExpect(jsonPath("$.students").isEmpty());

        assertThat(lecturerRepository.existsByLecturerId("L1")).isTrue();
    }

    @Test
    void createLecturer_whenAlreadyExists_returnsError() throws Exception {
        String body = json(LECTURER_CONTENT);

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
                        .content(json(Map.of(
                                KEY_LECTURER_ID, LECTURER_ID,
                                KEY_NAME, " ",
                                KEY_SURNAME, LECTURER_SURNAME
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString(" name ")));

        // Non-alphanumeric surname.
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                KEY_LECTURER_ID, LECTURER_ID,
                                KEY_NAME, LECTURER_NAME,
                                KEY_SURNAME, "Sur-name"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(" surname ")));

        assertThat(lecturerRepository.count()).isZero();
    }

    @Test
    void getLecturer_returnsLecturerWithAssignedStudents() throws Exception {
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(LECTURER_CONTENT)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(STUDENT_CONTENT)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/lecturers/L1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lecturerId").value(LECTURER_ID))
                .andExpect(jsonPath("$.students", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.students[0].studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.students[0].name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.students[0].surname").value(STUDENT_SURNAME));
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
