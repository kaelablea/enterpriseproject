package person.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.Date;

public class Audit {
    int id;
    String changeMsg;
    int changedBy;
    int personId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Date whenOccurred;

    //For adding audits
    public Audit(String changeMsg, int personId){
        this.changeMsg = changeMsg;
        this.personId = personId;
    }

    //When retrieving audits
    public Audit(int id, String changeMsg, int changedBy, int personId, Timestamp whenOccurred){
        this.id = id;
        this.changeMsg = changeMsg;
        this.changedBy = changedBy;
        this.personId = personId;
        this.whenOccurred = whenOccurred;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChangeMsg() {
        return changeMsg;
    }

    public void setChangeMsg(String changeMsg) {
        this.changeMsg = changeMsg;
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public Timestamp getWhenOccurred() {
        return (Timestamp) whenOccurred;
    }

    public void setWhenOccurred(Timestamp whenOccurred) {
        this.whenOccurred = whenOccurred;
    }
}
