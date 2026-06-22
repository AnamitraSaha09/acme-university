package com.acme.university.web.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for "Create Lecturer".
 * lecturerId, name and surname are expected to be ata max 255 alphanumeric characters and cannot be blank/null
 */
public record CreateLecturerRequest(

        @NotBlank(message = "Lecturer lecturerId cannot be null/blank.")
        @Size(max = 255, message = "Lecturer lecturerId must be at most 255 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Lecturer lecturerId must be alphanumeric")
        String lecturerId,

        @NotBlank(message = "Lecturer name cannot be null/blank.")
        @Size(max = 255, message = "Lecturer name must be at most 255 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Lecturer name must be alphanumeric")
        String name,

        @NotBlank(message = "Lecturer surname cannot be null/blank.")
        @Size(max = 255, message = "Lecturer surname must be at most 255 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Lecturer surname must be alphanumeric")
        String surname) {
}
