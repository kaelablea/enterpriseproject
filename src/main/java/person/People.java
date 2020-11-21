package person;

import java.util.ArrayList;

public class People {
    ArrayList<Person> people;

    public People(){
        this.people= new ArrayList<>();
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    public void setPeople(ArrayList<Person> people) {
        this.people = people;
    }
}
