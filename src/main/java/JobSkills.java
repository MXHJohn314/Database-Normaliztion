/*
 * CS3810 - Principles of Database Systems - Spring 2021
 * Instructor: Thyago Mota
 * Description: DB 02 - JobSkills
 * Student(s) Name(s):
 */

import java.io.*;
import java.sql.*;
import java.util.*;

public class JobSkills {

    public static String DATASET = "data/job_skills.csv";
    public static String PROPERTIES = "config.properties";
    private static Connection conn;
    private static Statement stmt;

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        // TODOd: load database properties
        Properties prop = new Properties();
        prop.load(new FileInputStream(PROPERTIES));
        String server = prop.getProperty("server");
        String schema = prop.getProperty("database");
        String username = prop.getProperty("user");
        String password = prop.getProperty("password");

        // TODOd: connect to the database
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://" + server + ":3306/" + schema ;
        conn = DriverManager.getConnection(url,username, password);
    
        // Only for testing purposes
         jdbcInit();
    
        // TODOd: complete the data load
        splitCsv(System.getProperty("user.dir") + "/", DATASET);
    }
    
    // Helper method for testing
    private static void jdbcInit() throws SQLException {
            String[] SQL_COMMANDS = new String[] {
                "drop database if exists jobs",
            
                "create database if not exists jobs",
            
                "use jobs",
            
                "create table jobs (" +
                        "id int primary key," +
                        "title varchar(255) not null" +
                        ")",
            
                "create table skills (" +
                        "id int primary key," +
                        "name varchar(255) not null" +
                        ")",
            
                "create table job_skills (" +
                        "job_id int not null," +
                        "skill_id int not null," +
                        "constraint pk_job_skills  primary key (job_id, skill_id)," +
                        "foreign key (job_id)	    references   jobs	(id)," +
                        "foreign key (skill_id)     references  skills	(id)" +
                        ")",
                };
        for (int i = 0; i < SQL_COMMANDS.length; i++) {
            stmt = conn.createStatement();
            stmt.executeUpdate(SQL_COMMANDS[i]);
        }
    }
    
    public static void splitCsv(String workingDir, String fileName) throws IOException, SQLException {
        File oldJobsFile = new File(workingDir + DATASET);
        Scanner sc = new Scanner(oldJobsFile);
        PreparedStatement insertJobs = conn.prepareStatement("INSERT ignore INTO jobs (id, title) values (?, ?)");
        PreparedStatement insertSkills = conn.prepareStatement("INSERT ignore INTO skills (id, name) values (?, ?)");
        PreparedStatement insertJobSkills = conn.prepareStatement("INSERT ignore INTO job_skills (job_id, skill_id) values (?, ?)");
        conn.setAutoCommit(false);
        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] jobAndRest = line.split(",");
            String[] jobAndId = jobAndRest[0].split(":");
            int jobId = Integer.parseInt(jobAndId[0]);
            String jobTitle = jobAndId[1];
            String[] skillsArray = jobAndRest[1].split(";");
            insertJobs.setInt(1, jobId);
            insertJobs.setString(2,jobTitle);
            insertJobs.addBatch();
            for(String skillAndId: skillsArray) {
                String[] skill = skillAndId.split(":");
                int skillId = Integer.parseInt(skill[0]);
                String skillName = skill[1];
                insertSkills.setInt(1, skillId);
                insertSkills.setString(2, skillName);
                insertJobSkills.setInt(1, jobId);
                insertJobSkills.setInt(2, skillId);
                insertSkills.addBatch();
                insertJobSkills.addBatch();
            }
        }
        insertJobs.executeBatch();
        insertSkills.executeBatch();
        insertJobSkills.executeBatch();
        conn.commit();
        conn.setAutoCommit(true);
    }

    private static ResultSet query(String sql) throws SQLException {
        return stmt.executeQuery(sql);
    }

    private static int update(String sql) throws SQLException {
        return stmt.executeUpdate(sql);
    }
}
