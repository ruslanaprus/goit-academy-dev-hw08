SELECT name, salary AS highest_salary
FROM worker 
WHERE salary IN (SELECT MAX(salary) FROM worker);