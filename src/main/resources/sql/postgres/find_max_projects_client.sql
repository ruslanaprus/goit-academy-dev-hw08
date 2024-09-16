-- Task 4

WITH project_counts AS (
	SELECT client_id, COUNT(*) AS project_count
	FROM project
	GROUP BY client_id
)
SELECT
	client.name AS client_name,
	project_counts.project_count
FROM project_counts
JOIN client ON project_counts.client_id = client.id
WHERE project_counts.project_count = (
		SELECT MAX(project_count)
		FROM project_counts
);