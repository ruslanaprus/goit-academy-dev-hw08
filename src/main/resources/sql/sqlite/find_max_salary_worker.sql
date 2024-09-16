-- Task 3
SELECT name, salary AS highest_salary
FROM worker
WHERE salary = (SELECT MAX(salary) FROM worker);