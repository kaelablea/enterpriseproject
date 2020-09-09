package person;

/**
 * Created by Mikaela on 9/8/2020.
 */

import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

public class Person {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private int id;
    private int age;

    public Person() {
    }

    public Person(int id, String firstName, String lastName, LocalDate dateOfBirth, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.id = id;
        this.age = age;

        logger.debug("person constructed");
    }

    // throw an exception if anything bad happens during save process
    // wrap all other exceptions in a widget exception (unchecked so caller is NOT required to try/catch it)
    public void save() throws PersonException {
        try {
            logger.debug("saving person");
        } catch(Exception e) {
            logger.error(e);
            throw new PersonException(e);
        }
    }

    // validators
    //Probably won't want negative IDs
    public static boolean isValidID(int id) {
        if(id < 0)
            return false;
        return true;
    }

    // accessors

    public String toString(){return firstName + " " + lastName;}

    public static Logger getLogger() {
        return logger;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


}
