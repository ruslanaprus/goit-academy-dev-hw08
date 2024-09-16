-- Task 2

INSERT INTO worker (name, birthday, email, level, salary)
VALUES
('Alice', '2001-08-20', 'alice@example.com','Senior', 100000),
('Bob', '1995-10-11', 'bob@example.com','Middle', 20000),
('Eve', '2000-01-01', 'eve@example.com','Senior', 32000),
('Whiskers', '2015-06-01', 'whiskers@example.com','Trainee', 10000),
('Purrito', '2020-03-21', 'purrito@example.com','Junior', 15000),
('Pawsters', '2021-01-30', 'pawsters@example.com','Trainee', 950),
('Meowiarty', '2021-01-30', 'meowiarty@example.com','Senior', 29000),
('Purrlock', '2011-07-12', 'purrlock@example.com','Middle', 22000),
('Clawster', '2019-04-01', 'clawster@example.com','Middle', 22000),
('Buttercup', '2020-09-27', 'buttercup@example.com','Middle', 32000),
('ET', '1901-01-01', 'et@example.com','Senior', 100000)
ON CONFLICT DO NOTHING;

INSERT INTO client (name)
VALUES 
('Whiskers and Paw Co.'),
('Purrfect Solutions'),
('Meowster Inc.'),
('Clawtastic Creations'),
('Snack Caterprises')
ON CONFLICT DO NOTHING;

INSERT INTO project (name, client_id, start_date, finish_date)
VALUES
-- Project 1: 10 months duration
('Purrfectly Crafted', 1, '2023-01-01', '2023-10-31'),
-- Project 2: 24 months duration
('Whisker Wonderland', 2, '2022-05-15', '2024-05-15'),
-- Project 3: 6 months duration
('Meowgical Moments', 3, '2023-02-01', '2023-07-31'),
-- Project 4: 101 months duration
('Pawsitively Adorable Designs', 4, '2015-06-01', '2023-10-01'),
-- Project 5: 18 months duration
('Cattitude Chronicles', 5, '2021-08-01', '2023-02-01'),
-- Project 6: 3 months duration
('Feline Fine Art', 3, '2023-06-01', '2023-08-31'),
-- Project 7: 12 months duration
('The Whisker Whisperer', 2, '2022-07-01', '2023-06-30'),
-- Project 8: 50 months duration
('Paw Prints & Paintbrushes', 3, '2018-01-01', '2022-02-28'),
-- Project 9: 2 months duration
('Meowsterpiece Gallery', 4, '2023-07-01', '2023-08-31'),
-- Project 10: 7 months duration
('Fur-tastic Finds', 4, '2023-03-01', '2023-09-30'),
-- Project 4: 101 months duration
('Cat-astrophic Cuteness', 1, '2015-07-01', '2023-10-31')
ON CONFLICT DO NOTHING;

INSERT INTO project_worker (project_id, worker_id) VALUES
-- Project 1 assignments
(1, 1), (1, 2), (1, 3),
-- Project 2 assignments
(2, 4), (2, 5),
-- Project 3 assignments
(3, 6), (3, 7), (3, 8),
-- Project 4 assignments
(4, 9), (4, 10), (4, 11),
-- Project 5 assignments
(5, 1), (5, 4), (5, 7),
-- Project 6 assignments
(6, 2), (6, 5),
-- Project 7 assignments
(7, 3), (7, 6),
-- Project 8 assignments
(8, 8), (8, 9),
-- Project 9 assignments
(9, 10), (9, 11),
-- Project 10 assignments
(10, 1), (10, 3), (10, 5)
ON CONFLICT DO NOTHING;