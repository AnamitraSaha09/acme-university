package com.acme.university;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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

    private void createLecturer(String id, String name, String surname) throws Exception {
        mockMvc.perform(post("/api/v1/lecturers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(KEY_LECTURER_ID, id, KEY_NAME, name, KEY_SURNAME, surname))))
                .andExpect(status().isCreated());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void addStudent_toExistingLecturer_createsAndAssigns() throws Exception {
        createLecturer(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME);

        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(STUDENT_CONTENT)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.surname").value(STUDENT_SURNAME))
                .andExpect(jsonPath("$.lecturers", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.lecturers[0].lecturerId").value(LECTURER_ID));

        assertThat(studentRepository.existsByStudentId(STUDENT_ID)).isTrue();
    }

    @Test
    void addStudent_whenStudentAlreadyExists_assignsExistingStudent() throws Exception {
        createLecturer(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME);
        createLecturer("L2", "NameL2", "SurnameL2");

        // First assignment creates the student.
        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(STUDENT_CONTENT)))
                .andExpect(status().isCreated());

        // Second assignment reuses the existing student.
        mockMvc.perform(post("/api/v1/lecturers/L2/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(STUDENT_CONTENT)))
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
                        .content(json(STUDENT_CONTENT)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    void addStudent_withInvalidName_returnsBadRequest() throws Exception {
        createLecturer(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME);

        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(KEY_STUDENT_ID, "S1", KEY_NAME, "", KEY_SURNAME, "SurnameS1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(" name ")));

        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(KEY_STUDENT_ID, "S1", KEY_NAME, "NameS1", KEY_SURNAME, "Sur name"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(" surname ")));

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    void getStudent_returnsStudentWithAssignedLecturers() throws Exception {
        createLecturer(LECTURER_ID, LECTURER_NAME, LECTURER_SURNAME);
        mockMvc.perform(post("/api/v1/lecturers/L1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(STUDENT_CONTENT)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/students/S1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(STUDENT_ID))
                .andExpect(jsonPath("$.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.surname").value(STUDENT_SURNAME))
                .andExpect(jsonPath("$.lecturers", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.lecturers[0].lecturerId").value(LECTURER_ID));
    }

    @Test
    void getStudent_whenNotFound_returnsError() throws Exception {
        mockMvc.perform(get("/api/v1/students/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
