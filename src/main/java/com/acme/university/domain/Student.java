package com.acme.university.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * A student at ACME University.
 *
 * <p>A student can have many lecturers, and a lecturer can be assigned to many
 * students (many-to-many relationship).
 *
 * <p>Only the minimal mapping needed for the application to boot is provided.
 * <ul>
 *   <li>add a {@code @Table} if you want a custom table name;</li>
 *   <li>add column constraints as appropriate, e.g.
 *       {@code @Column(nullable = false, unique = true)} on {@code studentId};</li>
 *   <li>map the inverse side of the many-to-many relationship to
 *       {@link Lecturer}, consistent with whatever you declare there.</li>
 * </ul>
 */
@Entity
@Table(name = "student")
public class Student {

    /** Surrogate key (optional design choice — map or remove as you see fit). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Business identifier supplied by the API client. Must be unique. */
    @Column(name = "student_id", unique = true, nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    private Set<Lecturer> lecturers = new HashSet<>();

    protected Student() {
        // Required by JPA.
    }

    public Student(String studentId, String name, String surname) {
        this.studentId = studentId;
        this.name = name;
        this.surname = surname;
    }

    public Long getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Set<Lecturer> getLecturers() { return this.lecturers; }
}
