-- Task 1

CREATE TABLE IF NOT EXISTS worker (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL CHECK (length(name) BETWEEN 2 AND 1000),
    birthday TEXT NOT NULL CHECK (birthday >= '1901-01-01'),
    email TEXT NOT NULL UNIQUE,
	level TEXT NOT NULL CHECK (level IN ('trainee', 'junior', 'middle', 'senior')),
	salary INTEGER NOT NULL CHECK (salary BETWEEN 100 AND 100000)
);

CREATE TABLE IF NOT EXISTS client (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL UNIQUE CHECK (length(name) BETWEEN 2 AND 1000)
);

CREATE TABLE IF NOT EXISTS project (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT UNIQUE,
	client_id INTEGER NOT NULL,
	start_date TEXT,
	finish_date TEXT CHECK (date(finish_date) >= date(start_date)),
	FOREIGN KEY (client_id) REFERENCES client(id)
);

CREATE TABLE IF NOT EXISTS project_worker (
	project_id INTEGER NOT NULL,
	worker_id INTEGER NOT NULL,
	PRIMARY KEY (project_id, worker_id),
	FOREIGN KEY (project_id) REFERENCES project(id),
	FOREIGN KEY (worker_id) REFERENCES worker(id)
);