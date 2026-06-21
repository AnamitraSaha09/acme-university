-- Index for lookup by student on the join table.

CREATE INDEX idx_assignment_student_id ON assignment (student_id);