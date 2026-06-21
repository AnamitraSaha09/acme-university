package com.acme.university.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A lecturer at ACME University.
 *
 * <p>A lecturer can be assigned to many students, and a student can have many
 * lecturers (many-to-many relationship).
 *
 * <p>Only the minimal mapping needed for the application to boot is provided.
 * complete the JPA mapping:
 * <ul>
 *   <li>add a {@code @Table} if you want a custom table name;</li>
 *   <li>add column constraints as appropriate, e.g.
 *       {@code @Column(nullable = false, unique = true)} on {@code lecturerId};</li>
 *   <li>map the many-to-many relationship to {@link Student} (owning side, join
 *       table, fetch strategy) and add a helper to keep both sides in sync.</li>
 * </ul>
 */
@Entity
@Table(name = "lecturer")
public class Lecturer {

    /**
     * Surrogate key (optional design choice — map or remove as you see fit).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Business identifier supplied by the API client. Must be unique.
     */
    @Column(name = "lecturer_id", unique = true, nullable = false)
    private String lecturerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "assignment",
            joinColumns = @JoinColumn(
                    name = "lecturer_id",
                    foreignKey = @ForeignKey(
                            name = "fk_assignment_lecturer"
                    )
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "student_id",
                    foreignKey = @ForeignKey(
                            name = "fk_assignment_student"
                    )
            ),
            indexes = @Index(name = "idx_assignment_student", columnList = "student_id")
    )
    private Set<Student> students = new HashSet<>();


    protected Lecturer() {
        // Required by JPA.
    }

    public Lecturer(String lecturerId, String name, String surname) {
        this.lecturerId = lecturerId;
        this.name = name;
        this.surname = surname;
    }

    public Long getId() {
        return id;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
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

    public Set<Student> getStudents() {
        return students;
    }

    public void assignUniqueStudent(Student student) {
        assignUniqueStudents(Set.of(student));
    }

    public void assignUniqueStudents(Set<Student> newStudents) {
        newStudents.forEach(student -> {
            if (this.students.add(student)) {
                student.getLecturers().add(this);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lecturer)) return false;
        return (id.equals(((Lecturer) o).id) || lecturerId.equals(((Lecturer) o).getLecturerId()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(lecturerId);
    }
}
