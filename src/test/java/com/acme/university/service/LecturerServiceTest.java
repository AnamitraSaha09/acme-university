package com.acme.university.service;

import com.acme.university.domain.Lecturer;
import com.acme.university.exception.ResourceAlreadyExistsException;
import com.acme.university.exception.ResourceNotFoundException;
import com.acme.university.repository.LecturerRepository;
import com.acme.university.web.dto.CreateLecturerRequest;
import com.acme.university.web.dto.LecturerResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test with repository mocked
 */
@ExtendWith(MockitoExtension.class)
public class LecturerServiceTest {

    @Mock
    private LecturerRepository lecturerRepository;

    @InjectMocks
    private LecturerService lecturerService;

    @Test
    void createLecturer_newLecturer() {
        when(lecturerRepository.existsByLecturerId("L1")).thenReturn(false);
        when(lecturerRepository.save(any(Lecturer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LecturerResponse response = lecturerService.createLecturer(
                new CreateLecturerRequest("L1", "Name", "Surname"));

        assertThat(response.lecturerId()).isEqualTo("L1");
        assertThat(response.name()).isEqualTo("Name");
        assertThat(response.surname()).isEqualTo("Surname");
        assertThat(response.students()).isEmpty();
        verify(lecturerRepository).save(any(Lecturer.class));
    }

    @Test
    void createLecturer_whenAlreadyExists() {
        when(lecturerRepository.existsByLecturerId("L1")).thenReturn(true);

        assertThatThrownBy(() -> lecturerService.createLecturer(
                new CreateLecturerRequest("L1", "Name", "Surname")))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(lecturerRepository, never()).save(any());
    }

    @Test
    void getLecturer_returnsResponse() {
        when(lecturerRepository.findByLecturerId("L1"))
                .thenReturn(Optional.of(new Lecturer("L1", "Name", "Surname")));

        LecturerResponse response = lecturerService.getLecturer("L1");

        assertThat(response.lecturerId()).isEqualTo("L1");
        assertThat(response.students()).isEmpty();
    }

    @Test
    void getLecturer_whenMissing() {
        when(lecturerRepository.findByLecturerId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lecturerService.getLecturer("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
