package person.db;

import com.mysql.cj.jdbc.MysqlDataSource;
import person.main.App;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnect {
    public static Connection connectToDB() throws SQLException, IOException {
        //connect to data source and create a connection instance
        Properties props = getConfig("/db.properties");

        //create the datasource
        MysqlDataSource ds = new MysqlDataSource();
        ds.setURL(props.getProperty("MYSQL_DB_URL"));
        ds.setUser(props.getProperty("MYSQL_DB_USERNAME"));
        ds.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));

        //create the connection
        return ds.getConnection();
    }

    private static Properties getConfig(String propsFileName) throws IOException {
        Properties props = new Properties();

        BufferedInputStream propsFile = (BufferedInputStream) App.class.getResourceAsStream(propsFileName);
        props.load(propsFile);
        propsFile.close();

        return props;
    }
}
