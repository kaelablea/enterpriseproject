package person.models;

import java.util.ArrayList;

public class AuditTrail {
    ArrayList<Audit> audits;

    public AuditTrail(){
        this.audits = new ArrayList<Audit>();
    }

    public ArrayList<Audit> getAudits() {
        return audits;
    }

    public void setAudits(ArrayList<Audit> audits) {
        this.audits = audits;
    }
}
