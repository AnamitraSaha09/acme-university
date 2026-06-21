package com.acme.university.web;

import com.acme.university.service.LecturerService;
import com.acme.university.service.StudentService;
import com.acme.university.web.dto.CreateLecturerRequest;
import com.acme.university.web.dto.CreateStudentRequest;
import com.acme.university.web.dto.LecturerResponse;
import com.acme.university.web.dto.StudentResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST resource for lecturers (and assigning students to a lecturer).
 *
 * <p>Implement the endpoints. All endpoints must consume and produce
 * JSON. You decide the request mappings, HTTP methods, status codes, request
 * bodies, and path/parameter binding.
 * <ul>
 *   <li><b>Create Lecturer</b> — create a lecturer if it doesn't already exist,
 *       otherwise return an appropriate error.</li>
 *   <li><b>Get Lecturer</b> — return a lecturer (by lecturerId) with their
 *       assigned students.</li>
 *   <li><b>Add Student</b> — create the student (if not present) and assign it
 *       to an existing lecturer; error if the lecturer does not exist.</li>
 * </ul>
 * Delegate the work to {@link LecturerService} / {@link StudentService}, and
 * remember to validate request bodies (see the {@code web.dto} package).
 */
@RestController
@RequestMapping(path = "/api/v1/lecturers")
public class LecturerController {

    private final LecturerService lecturerService;
    private final StudentService studentService;

    public LecturerController(LecturerService lecturerService, StudentService studentService) {
        this.lecturerService = lecturerService;
        this.studentService = studentService;
    }

    @GetMapping("/{lecturerId}")
    public ResponseEntity<LecturerResponse> getLecturer(@PathVariable String lecturerId) {
        return ResponseEntity.ok(lecturerService.getLecturer(lecturerId));
    }

    @PostMapping
    public ResponseEntity<LecturerResponse> createLecturer(@Valid @RequestBody CreateLecturerRequest lecturerRequest) {
        LecturerResponse lecturer =  lecturerService.createLecturer(lecturerRequest);
        return ResponseEntity.created(URI.create("/api/v1/lecturers")).body(lecturer);
    }

    @PostMapping("/{lecturerId}/students")
    public ResponseEntity<StudentResponse> addStudent(
            @PathVariable String lecturerId,
            @Valid @RequestBody CreateStudentRequest studentRequest
            ) {
        StudentResponse student = studentService.addStudentToLecturer(lecturerId, studentRequest);
        return ResponseEntity.created(URI.create("/api/v1/students")).body(student);
    }
}
