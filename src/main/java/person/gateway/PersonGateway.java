package person.gateway;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import person.People;
import person.Person;
import person.PersonException;
import person.db.DBConnect;
import person.fx.SessionParameters;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.mysql.cj.conf.PropertyKey.logger;

public class PersonGateway {


    private static Connection connection;
    private static Logger logger = LogManager.getLogger();


    public PersonGateway(Connection connection) {

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
            person.setId(id);
            person.setFirstName(rs.getString("first_name"));
            person.setLastName(rs.getString("last_name"));
            person.setDateOfBirth(rs.getDate("dob").toLocalDate());
            st.close();
            return person;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public People fetchPeople(){
        PreparedStatement st = null;
        ResultSet rs = null;
        People people = new People();
        ArrayList<Person> list = people.getPeople();
        try{
            st = connection.prepareStatement("SELECT * FROM `Person` WHERE 1");
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


    public static void insertPerson(Person newPerson){

        PreparedStatement st = null;
        ResultSet rs = null;
        logger.info(newPerson.toString());
        try {
            st = connection.prepareStatement("INSERT INTO `Person`(`first_name`, `last_name`,  `dob`) VALUES (?,?,?)");
            st.setString(1,newPerson.getFirstName());
            st.setString(2, newPerson.getLastName());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = format.parse(newPerson.getDateOfBirth().toString());
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            st.setDate(3, sqlDate);
            st.executeUpdate();
            st.close();

        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return;
    }


    public static void updatePerson( Map<String,String> updateValues, int id) {

        logger.info("updating person with id " + id);
        logger.info(updateValues.toString());
        PreparedStatement st = null;
        int rs;
        Time time = null;
        try {
            if(updateValues.containsKey("firstName")) {
                st = connection.prepareStatement("UPDATE `Person` SET `first_name`=? WHERE `id` =?");
                st.setInt(2, id);
                st.setString(1, updateValues.get("firstName"));
                rs = st.executeUpdate();
            }
            if (updateValues.containsKey("lastName")) {
                st = connection.prepareStatement("UPDATE `Person` SET `last_name`=? WHERE `id` =?");
                st.setInt(2, id);
                st.setString(1, updateValues.get("lastName"));
                rs=st.executeUpdate();
            }
            if (updateValues.containsKey("dateOfBirth")) {
                st = connection.prepareStatement("UPDATE `Person` SET `dob`=? WHERE `id` =?");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = format.parse(updateValues.get("dateOfBirth"));
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                st.setDate(1, sqlDate);
                st.setInt(2, id);
                rs = st.executeUpdate();
            }
            st.close();
            return;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }
        return;
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
