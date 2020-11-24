package person.backend.repositories;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import person.models.Audit;

import java.sql.*;
import java.util.ArrayList;

public class AuditRepo {

    private static Connection connection;
    private static Logger logger = LogManager.getLogger();


    public AuditRepo(Connection connection) {
        this.connection = connection;
    }

    public static ArrayList<Audit> getAuditTrail(int id){
        PreparedStatement st = null;
        ResultSet rs = null;
        ArrayList<Audit> auditTrail = new ArrayList<Audit>();
        try {
            st = connection.prepareStatement("SELECT * FROM `Audit` WHERE `person_id`=?");
            st.setInt(1, id);
            rs = st.executeQuery();

            while(rs.next()){
                int auditId = rs.getInt("id");
                String changeMsg = rs.getString("change_msg");
                int changedBy = rs.getInt("changed_by");
                int personId = rs.getInt("person_id");
                Timestamp whenOccurred = rs.getTimestamp("when_occurred");
                logger.info(auditId);
                Audit audit = new Audit(auditId, changeMsg, changedBy, personId, whenOccurred);
                logger.info(audit.getWhenOccurred().toString());
                auditTrail.add(audit);
            }
            st.close();
            return auditTrail;

         } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public static void addAudit(Audit audit, String token){
        PreparedStatement st = null;
        ResultSet rs;
        int valid;

        try {
            st = connection.prepareStatement("SELECT `id` FROM `Session` WHERE `token`=?");
            st.setString(1,token);
            rs = st.executeQuery();
            rs.first();
            int id = rs.getInt("id");

            st = connection.prepareStatement("INSERT INTO `Audit`(`change_msg`, `changed_by`, `person_id`) VALUES (?,?,?)");
            st.setString(1, audit.getChangeMsg());
            st.setInt(2, id);
            st.setInt(3, audit.getPersonId());
            valid = st.executeUpdate();
            st.close();

            } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

    }

}
