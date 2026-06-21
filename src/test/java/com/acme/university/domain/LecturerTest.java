package com.acme.university.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LecturerTest {
    @Test
    void assignStudent_linksBothSides() {
        Lecturer lecturer = new Lecturer("L1", "Ada", "Lovelace");
        Student student = new Student("S1", "Alan", "Turing");

        lecturer.assignUniqueStudent(student);

        assertThat(lecturer.getStudents()).containsExactly(student);
        assertThat(student.getLecturers()).containsExactly(lecturer);
    }

    @Test
    void assignStudent_isIdempotent() {
        Lecturer lecturer = new Lecturer("L1", "Ada", "Lovelace");
        Student student = new Student("S1", "Alan", "Turing");

        lecturer.assignUniqueStudent(student);
        lecturer.assignUniqueStudent(student);

        assertThat(lecturer.getStudents()).hasSize(1);
        assertThat(student.getLecturers()).hasSize(1);
    }

    @Test
    void equalsAndHashCode_useBusinessId() {
        Lecturer one = new Lecturer("L1", "Ada", "Lovelace");
        Lecturer sameId = new Lecturer("L1", "Different", "Person");
        Lecturer otherId = new Lecturer("L2", "Ada", "Lovelace");

        assertThat(one).isEqualTo(sameId);
        assertThat(one).hasSameHashCodeAs(sameId);
        assertThat(one).isNotEqualTo(otherId);
    }
}
