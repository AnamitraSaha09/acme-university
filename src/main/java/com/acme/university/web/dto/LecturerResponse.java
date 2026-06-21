package com.acme.university.web.dto;

import com.acme.university.domain.Lecturer;

import java.util.List;

/**
 * Response body for "Get Lecturer": the lecturer's identity plus the list of
 * assigned students.
 */
public record LecturerResponse(
        String lecturerId,
        String name,
        String surname,
        List<AssignedStudent> students) {

    /** Minimal view of a student assigned to the lecturer. */
    public record AssignedStudent(
            String studentId,
            String name,
            String surname) {
    }

    public static LecturerResponse fromLecturer(Lecturer lecturer) {
        List<AssignedStudent> assignedStudents = lecturer.getStudents().stream().map(
                student -> new AssignedStudent(student.getStudentId(), student.getName(), student.getSurname())
        ).toList();
        return new LecturerResponse(lecturer.getLecturerId(), lecturer.getName(), lecturer.getSurname(), assignedStudents);
    }
}
