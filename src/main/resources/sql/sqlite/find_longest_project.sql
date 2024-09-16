-- Task 5
WITH project_durations AS (
	SELECT
		name,
		start_date,
		finish_date,
		(julianday(finish_date) - julianday(start_date)) / 30 AS duration_in_months
	FROM project
)
SELECT name, duration_in_months
FROM project_durations
WHERE duration_in_months = (
	SELECT MAX(duration_in_months)
	FROM project_durations
);