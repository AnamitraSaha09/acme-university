package com.acme.university.web.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for "Add Student"
 * studentId, name and surname are expected to be ata max 255 alphanumeric characters and cannot be blank/null
 */
public record CreateStudentRequest(

        @NotBlank(message = "Student studentId cannot be null/blank.")
        @Size(max = 255, message = "Student studentId must be at most 255 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Student studentId must be alphanumeric")
        String studentId,

        @NotBlank(message = "Student name cannot be null/blank.")
        @Size(max = 255, message = "Student name must be at most 255 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Student name must be alphanumeric")
        String name,

        @NotBlank(message = "Student surname cannot be null/blank.")
        @Size(max = 255, message = "Student surname must be at most 255 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Student surname must be alphanumeric")
        String surname) {
}
