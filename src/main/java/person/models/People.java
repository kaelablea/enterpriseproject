package person.models;

import java.util.ArrayList;
import java.util.List;

public class People {
    ArrayList<Person> people;
    private int pageSize;
    private int currentPage;
    private int totalRecords;

    public People(){
        this.people= new ArrayList<>();
        this.pageSize = 10;
    }

    public People(List<Object> toList) {
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    public void setPeople(ArrayList<Person> people) {
        this.people = people;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
}
