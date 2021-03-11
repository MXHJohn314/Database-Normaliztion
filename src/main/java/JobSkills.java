/*
 * CS3810 - Principles of Database Systems - Spring 2021
 * Instructor: Thyago Mota
 * Description: DB 02 - JobSkills
 * Student(s) Name(s): Malcolm Johnson, Dakota Miller, Adam Wojdyla
 */

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public class JobSkills {
	public static final int FILE_NAME_INDEX = 0;
	public static final int TABLE_NAME_INDEX = 1;
	private static Connection connection;
	private static String DB_NAME;
	public static String DATASET = "data/job_skills.csv";
	public static int SQL_ITER = 0;
	public static final String[] SQL_COMMANDS = new String[] {
	 "drop database if exists jobs;",
	 
	 "create database if not exists jobs;",
	 
	 "use jobs;",
	 
	 "create table jobs (" +
	 "ID int primary key" +
	 ",descr varchar(255) not null" +
	 ")",
	 
	 "create table skills (" +
	 "ID int primary key" +
	 ",descr varchar(255) not null" +
	 ")",
	 
	 "create table job_skills (" +
	 "jobID int not null" +
	 ",skillID int not null" +
	 ",constraint pk_job_skills primary key (jobID, skillID)" +
	 ",foreign key (jobID)		references   jobs	(ID)" +
	 ",foreign key (skillID)    references   skills	(ID)" +
	 ") ",
	 
	 "SET GLOBAL local_infile=1",
	 };
	
	public static void main(String[] args)
	throws IOException, SQLException, ClassNotFoundException {
		
		String userDir = System.getProperty("user.dir");
		// TODO: load database properties
		Properties prop = loadDatabaseProperties(
		 userDir + "/config.properties");
		
		// TODO: connect to the database
		getConnection(prop);
		
		// TODO: complete the data load
		//Files.delete(Paths.get(PROGRAM_DATA_FILE));
		for(String sql: SQL_COMMANDS) {
			System.out.println(sql);
			connection.createStatement().executeUpdate(sql);
		}
		String[][] tables = splitCsv(userDir + "/data/", "jobs.csv");
		
		for(String[] tableParams: tables) {
			String sql = "LOAD DATA LOCAL INFILE ' "
				+ tableParams[FILE_NAME_INDEX].replace("\\", "/") + "'"
				+ " INTO TABLE " + tableParams[TABLE_NAME_INDEX]
				+ " fields terminated by ','\n"
				+ " optionally enclosed by '\"'\n"
				+ " escaped by '\"'\n"
				+ " LINES TERMINATED BY '\\r\\n'"
				+ " IGNORE 1 LINES";
			connection.createStatement().executeUpdate(sql);
		}
	}
	
	private static void getConnection(Properties prop)
	throws ClassNotFoundException, SQLException {
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		DB_NAME = prop.getProperty("schema");
		connection = DriverManager.getConnection(
		 "jdbc:mysql://"
		 + prop.getProperty("ip") + ":"
		 + prop.getProperty("port")
		 + "?allowLoadLocalInfile=true",
		 prop.getProperty("username"),
		 prop.getProperty("password")
		);
	}
	
	private static Properties loadDatabaseProperties(String propFileName)
	throws IOException {
		
		Properties prop = new Properties();
		InputStream stream = new FileInputStream(propFileName);
		if(stream != null) {
			prop.load(stream);
		}
		else {
			throw new FileNotFoundException(
			 "property file '" + propFileName + "' not found in the classpath");
		}
		return prop;
	}
	
	public static String[][] splitCsv(String workingDir, String fileName)
	throws IOException {
		
		File oldJobsFile = new File(workingDir + "jobs.csv");
		Scanner sc = new Scanner(oldJobsFile);
		File file1 = new File(workingDir + "jobs_updated.csv");
		FileWriter jobsFileWriter =
		 new FileWriter(file1);
		File file2 = new File(workingDir + "skills.csv");
		FileWriter skillsFileWriter =
		 new FileWriter(file2);
		File file3 = new File(workingDir + "job_skills.csv");
		FileWriter jobSkillsFileWriter =
		 new FileWriter(file3);
		jobsFileWriter.write("ID,descr" + "\n");
		skillsFileWriter.write("ID,descr" + "\n");
		jobSkillsFileWriter.write("jobID,skillID," + "\n");
		
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] jobAndRest = line.split(",");
			String[] jobAndId = jobAndRest[0].split(":");
			String jobId = jobAndId[0];
			String jobTitle = jobAndId[1];
			String[] skillsArray = jobAndRest[1].split(";");
			jobsFileWriter.write(jobId + "," + jobTitle + "\n");
			for(String skillAndId: skillsArray) {
				String[] skill = skillAndId.split(":");
				int skillId = Integer.parseInt(skill[0]);
				String skillName = skill[1];
				skillsFileWriter.write(skillId + "," + skillName + "\n");
				jobSkillsFileWriter.write(jobId + "," + skillId + "\n");
			}
		}
		sc.close();
		jobsFileWriter.close();
		skillsFileWriter.close();
		jobSkillsFileWriter.close();
		return new String[][] {
		 new String[] {file1.getAbsolutePath(), "jobs"},
		 new String[] {file2.getAbsolutePath(), "skills"},
		 new String[] {file3.getAbsolutePath(), "job_skills"},
		 };
	}
}
