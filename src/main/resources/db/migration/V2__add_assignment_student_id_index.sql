-- The composite PK (lecturer_id, student_id) makes lookups by lecturer fast,
-- but "find all lecturers for a given student" has no leading index column.
-- This index supports the reverse lookup used by GET /students/{id}.

CREATE INDEX idx_assignment_student_id ON assignment (student_id);