 DROP DATABASE IF EXISTS JOBS;
DROP USER IF EXISTS 'job_skills'@'%';
DROP USER IF EXISTS 'job_skills_admin'@'%';
CREATE DATABASE IF NOT EXISTS JOBS;
USE JOBS;

create table jobs (
	id int primary key,
	title varchar(255) not null
);

create table skills (
	id int primary key,
	name varchar(255) not null
);

create table job_skills (
	job_id int not null,
	skill_id int not null,
	constraint pk_job_skills  primary key (job_id, skill_id),
	foreign key (job_id)	    references   jobs	(id),
	foreign key (skill_id)     references   skills	(id)
);

-- Todod Check for 3NF.
-- Todod There should be 2 database users:
--      job_skills (with only SELECT capabilities on all tables) and
--      job_skills_admin (with full capabilities on all tables). 
CREATE USER 'job_skills'@'%';
GRANT SELECT ON jobs.* TO 'job_skills'@'%';
CREATE USER 'job_skills_admin'@'%';
GRANT ALL ON *.* TO 'job_skills_admin'@'%';

-- Todod a) what is the total number of job positions?
select count(id) AS 'Total # of job positions' from jobs;

-- Todod b) what is the total number of skills?
select count(id) AS 'Total # of skills' from skills;

-- Todod c) which job position titles have the word "database"?
select title AS 'Database like jobs' from jobs where title like '%database%';

-- Todod d) provide an alphabetical list of all job position titles that require "sql" or "mysql" as a skill.
select j.title from jobs j
inner join job_skills js on j.id = js.job_id
inner join skills s on js.skill_id = s.id
where s.name in ('sql', 'mysql')
order by 1
;

-- Todod e) which skills "database analyst"-like positions have that "database admin"-like positions don't? 
select an.skills AS "Analyst-like, non admin-like skills" from(
  select s.name as 'skills' from skills s
	inner join job_skills js on s.id = js.skill_id
	join jobs j on j.id = js.job_id
	where title like '%database analyst%') an
	where an.skills not in (
	  select s.name as 'skills' from skills s
		inner join job_skills js on s.id = js.skill_id
		join jobs j on j.id = js.job_id
		where title like '%database admin%')
group by an.skills
order by an.skills
;


-- Todod f) list the top 20 skills required by job positions having the word "database" in their titles.
select s2.name as 'skills'
from job_skills js
inner join skills s2
on js.skill_id = s2.id
       inner join jobs j on js.job_id = j.id
       and j.title like '%database%'
group by js.skill_id
order by js.skill_id
limit 20
;

select * from skills;

-- Todod Reflection
/*The skills provided below are the skills that we have gained knowledge on during this project:

computer science - relation algebra for queries.
git - collaborated with team member using git repository.
data entry/jdbc - sql batch (prepared) statements with code injection protection.
mysql/sql - most sql written thus far this semester.
troubleshooting - loading file (local vs non local infile) and setting up using mySql.
system design - the use of normalization for database table creation
*/




