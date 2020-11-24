package person.fx;

import person.models.Person;

public class PersonParameters {
    private static Person personParm;

    public static Person getPersonParm() {
        return personParm;
    }

    public static void setPersonParm(Person personParm) {
        PersonParameters.personParm = personParm;
    }


}