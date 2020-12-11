package person.backend.repositories;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.models.Audit;
import person.models.People;
import person.models.Person;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

public class PersonRepo {


    private static Connection connection;
    private static Logger logger = LogManager.getLogger();


    public PersonRepo(Connection connection) {

        this.connection = connection;
    }

    public static Person getPerson(int id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        Person person= new Person();
        try{
            st = connection.prepareStatement("SELECT * FROM `Person` WHERE `id`=?");
            st.setInt(1, id);
            rs = st.executeQuery();
            rs.first();
            person.setId(id);
            person.setFirstName(rs.getString("first_name"));
            person.setLastName(rs.getString("last_name"));
            person.setDateOfBirth(rs.getDate("dob").toLocalDate());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp ts = rs.getTimestamp("last_modified");
            person.setLastModified(ts);
            st.close();
            return person;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    //This is the initial fetch people, gets the first 10 person records
    public People fetchPeople(){
        PreparedStatement st = null;
        ResultSet rs = null;
        People people = new People();
        ArrayList<Person> list = people.getPeople();
        int numOfPeople = 0;
        try{
            st = connection.prepareStatement("SELECT * FROM `Person` WHERE 1");
            rs = st.executeQuery();
            rs.last();
            numOfPeople = rs.getRow();
            people.setTotalRecords(numOfPeople);
            st = connection.prepareStatement("SELECT * FROM `Person` WHERE 1 LIMIT ?, ?");
            st.setInt(1, 0);
            st.setInt(2, 10);
            rs = st.executeQuery();

                while (rs.next()) {
                    String fName = rs.getString("first_name");
                    logger.info("Person name: " + fName);
                    String lName = rs.getString("last_name");
                    LocalDate dob = rs.getDate("dob").toLocalDate();
                    int id = rs.getInt("id");
                    Person nextPerson = new Person(id, fName, lName, dob);
                    list.add(nextPerson);
                }
            people.setPageSize(10);
            people.setCurrentPage(1);
            people.setPeople(list);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                st.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return people;
        }
    }

    //This is getting the page specified of all person records
    public People fetchPeople(int page){
        PreparedStatement st = null;
        ResultSet rs = null;
        People people = new People();
        ArrayList<Person> list = people.getPeople();
        int numOfPeople =0;
        try {
            st = connection.prepareStatement("SELECT * FROM `Person` WHERE 1");
            rs = st.executeQuery();
            rs.last();
            numOfPeople = rs.getRow();
            people.setTotalRecords(numOfPeople);

            st = connection.prepareStatement("SELECT * FROM `Person` WHERE 1 LIMIT ?,?");
            if (page > 1){
                st.setInt(1, (page - 1) * 10+1);
            }
            else{
                st.setInt(1, 0);
            }
            st.setInt(2, 10);
            rs = st.executeQuery();

            while (rs.next()) {
                String fName = rs.getString("first_name");
                logger.info("Person name: " + fName);
                String lName = rs.getString("last_name");
                LocalDate dob = rs.getDate("dob").toLocalDate();
                int id = rs.getInt("id");
                Person nextPerson = new Person(id, fName, lName, dob);
                list.add(nextPerson);
            }
            people.setPageSize(10);
            people.setCurrentPage(page);
            people.setPeople(list);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                st.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return people;
        }
    }

    //this is getting the page of all records that have lastname starting with lastName
    public People fetchPeople(int page, String lastName){
        PreparedStatement st = null;
        ResultSet rs = null;
        People people = new People();
        ArrayList<Person> list = people.getPeople();
        int numOfPeople = 0;
        try{
            st = connection.prepareStatement("SELECT * FROM `Person` WHERE `last_name` LIKE ?");
            st.setString(1,lastName+"%");
            rs = st.executeQuery();
            rs.last();
            numOfPeople = rs.getRow();
            people.setTotalRecords(numOfPeople);

            st = connection.prepareStatement("SELECT * FROM `Person` WHERE `last_name` LIKE ? LIMIT ?, ?");
            st.setString(1,lastName+"%");
            if(page> 1) {
                st.setInt(2,(page-1) * 10 +1);
            }
            else{
                st.setInt(2, 0);
            }
            st.setInt(3, 10);
            rs = st.executeQuery();

            while (rs.next()) {
                String fName = rs.getString("first_name");
                logger.info("LName Person name: " + fName);
                String lName = rs.getString("last_name");
                LocalDate dob = rs.getDate("dob").toLocalDate();
                int id = rs.getInt("id");
                Person nextPerson = new Person(id, fName, lName, dob);
                list.add(nextPerson);
            }

            people.setPageSize(10);
            people.setCurrentPage(page);
            people.setPeople(list);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                st.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return people;
        }
    }


    public static Audit insertPerson(Person newPerson, String token){

        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = connection.prepareStatement("INSERT INTO `Person`(`first_name`, `last_name`,  `dob`) VALUES (?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setString(1,newPerson.getFirstName());
            st.setString(2, newPerson.getLastName());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = format.parse(newPerson.getDateOfBirth().toString());
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            st.setDate(3, sqlDate);
            st.executeUpdate();
            rs = st.getGeneratedKeys();

            rs.first();
            int personId = rs.getInt(1);
            String chngMsg = "added";
            Audit audit =new Audit(chngMsg,personId);
            st.close();
            return audit;

        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }


    public static ArrayList<String> updatePerson(Map<String,String> updateValues, int id) {

        logger.info("updating person with id " + id);
        logger.info(updateValues.toString());
        ArrayList<String> changeMsgs = new ArrayList<String>();
        PreparedStatement st = null;
        ResultSet rs;
        int valid =0;
        Time time = null;
        try {

            Person person = new Person();
            st = connection.prepareStatement("SELECT * FROM `Person` WHERE `id`=?");
            st.setInt(1,id);
            rs = st.executeQuery();
            rs.first();
            //logger.info(rs.toString());
            person.setFirstName(rs.getString("first_name"));
            person.setLastName(rs.getString("last_name"));
            person.setDateOfBirth(rs.getDate("dob").toLocalDate());

            if(updateValues.containsKey("firstName")) {
                st = connection.prepareStatement("UPDATE `Person` SET `first_name`=? WHERE `id` =?");
                st.setInt(2, id);
                st.setString(1, updateValues.get("firstName"));
                valid = st.executeUpdate();
                String msg = "first_name changed from " + person.getFirstName() + " to " + updateValues.get("firstName");
                changeMsgs.add(msg);

            }
            if (updateValues.containsKey("lastName")) {
                st = connection.prepareStatement("UPDATE `Person` SET `last_name`=? WHERE `id` =?");
                st.setInt(2, id);
                st.setString(1, updateValues.get("lastName"));
                valid=st.executeUpdate();
                String msg = "last_name changed from " + person.getLastName() + " to " + updateValues.get("lastName");
                changeMsgs.add(msg);
            }
            if (updateValues.containsKey("dateOfBirth")) {
                st = connection.prepareStatement("UPDATE `Person` SET `dob`=? WHERE `id` =?");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = format.parse(updateValues.get("dateOfBirth"));
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                st.setDate(1, sqlDate);
                st.setInt(2, id);
                valid = st.executeUpdate();
                String msg = "dob changed from " + person.getDateOfBirth().toString() + " to " + updateValues.get("dateOfBirth");
                changeMsgs.add(msg);
            }
            st.close();
            return changeMsgs;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }



    public static void deletePerson( int id){

        PreparedStatement st = null;
        ResultSet success = null;
        Time time = null;
        try {
            st = connection.prepareStatement("DELETE FROM `Person` WHERE `id`=?", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setInt(1,id);
            st.executeUpdate();
            st.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
     }



    //verify session token
    public static int validateSessionToken(String token) throws SQLException {

        PreparedStatement st = null;
        try {
            st = connection.prepareStatement("SELECT `username` FROM `Session` WHERE `token` = ?");
            st.setString(1,token);
            st.execute();
            return 200;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 401;
        }
    }

}
