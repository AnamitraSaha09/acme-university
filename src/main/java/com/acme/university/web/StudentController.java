package com.acme.university.web;

import com.acme.university.service.StudentService;
import com.acme.university.web.dto.StudentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST resource for students.
 *
 * <p>Implements the endpoint(s). All endpoints must consume and produce
 * JSON. You decide the request mapping, HTTP method, status codes, and
 * path/parameter binding.
 * <ul>
 *   <li><b>Get Student</b> — return a student (by studentId) with their
 *       assigned lecturers.</li>
 * </ul>
 * Delegate the work to {@link StudentService}.
 */
@RestController
@RequestMapping(path = "/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(studentService.getStudent(studentId));
    }
}
