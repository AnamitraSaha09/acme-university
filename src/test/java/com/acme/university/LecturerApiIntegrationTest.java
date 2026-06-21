package com.acme.university;

import static org.junit.jupiter.api.Assertions.fail;

import com.acme.university.repository.LecturerRepository;
import com.acme.university.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
        // TODO: ensure a clean slate between tests (the relationship may require
        //       deleting the join rows first depending on your mapping).
        studentRepository.deleteAll();
        lecturerRepository.deleteAll();
    }

    /*
    @Test
    void createLecturer_returnsCreatedLecturer() {
        // TODO: POST /api/lecturers with a valid body, assert success + body.
        fail("TODO: implement createLecturer_returnsCreatedLecturer");
    }

    @Test
    void createLecturer_whenAlreadyExists_returnsError() {
        // TODO: create a lecturer, POST the same id again, assert the error.
        fail("TODO: implement createLecturer_whenAlreadyExists_returnsError");
    }

    @Test
    void createLecturer_withInvalidName_returnsBadRequest() {
        // TODO: POST a body with a blank / non-alphanumeric name, assert HTTP 400.
        fail("TODO: implement createLecturer_withInvalidName_returnsBadRequest");
    }

    @Test
    void getLecturer_returnsLecturerWithAssignedStudents() {
        // TODO: create a lecturer (+ assigned students), GET it, assert the body.
        fail("TODO: implement getLecturer_returnsLecturerWithAssignedStudents");
    }

    @Test
    void getLecturer_whenNotFound_returnsError() {
        // TODO: GET an unknown lecturerId, assert HTTP 404.
        fail("TODO: implement getLecturer_whenNotFound_returnsError");
    }
     */
}
