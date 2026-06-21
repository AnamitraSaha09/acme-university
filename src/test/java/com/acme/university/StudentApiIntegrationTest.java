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
        // TODO: ensure a clean slate between tests.
        studentRepository.deleteAll();
        lecturerRepository.deleteAll();
    }

    /*
    @Test
    void addStudent_toExistingLecturer_createsAndAssigns() {
        // TODO: create a lecturer, POST /api/lecturers/{id}/students, assert success.
        fail("TODO: implement addStudent_toExistingLecturer_createsAndAssigns");
    }

    @Test
    void addStudent_whenStudentAlreadyExists_assignsExistingStudent() {
        // TODO: assign an already-existing student to a lecturer (no duplicate created).
        fail("TODO: implement addStudent_whenStudentAlreadyExists_assignsExistingStudent");
    }

    @Test
    void addStudent_toNonExistentLecturer_returnsError() {
        // TODO: POST to an unknown lecturerId, assert the error (e.g. HTTP 404).
        fail("TODO: implement addStudent_toNonExistentLecturer_returnsError");
    }

    @Test
    void addStudent_withInvalidName_returnsBadRequest() {
        // TODO: POST a body with a blank / non-alphanumeric name, assert HTTP 400.
        fail("TODO: implement addStudent_withInvalidName_returnsBadRequest");
    }

    @Test
    void getStudent_returnsStudentWithAssignedLecturers() {
        // TODO: create + assign, GET /api/students/{id}, assert the body.
        fail("TODO: implement getStudent_returnsStudentWithAssignedLecturers");
    }

    @Test
    void getStudent_whenNotFound_returnsError() {
        // TODO: GET an unknown studentId, assert HTTP 404.
        fail("TODO: implement getStudent_whenNotFound_returnsError");
    }

     */
}
