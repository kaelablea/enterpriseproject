package person.models;

import java.util.ArrayList;
import java.util.List;

public class People {
    ArrayList<Person> people;

    public People(){
        this.people= new ArrayList<>();
    }

    public People(List<Object> toList) {
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    public void setPeople(ArrayList<Person> people) {
        this.people = people;
    }
}
