-- Task 7
SELECT
	p.name AS project_name,
	COALESCE(SUM(w.salary), 0) * ((julianday(p.finish_date) - julianday(p.start_date)) / 30) AS project_price
FROM
	project p
	LEFT JOIN project_worker pw ON p.id = pw.project_id
	LEFT JOIN worker w ON pw.worker_id = w.id
GROUP BY
	p.id,
	p.name,
	p.finish_date,
	p.start_date
ORDER BY project_price DESC;
