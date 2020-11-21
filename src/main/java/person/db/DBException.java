package person.db;


public class DBException extends RuntimeException {
    public DBException(String msg) {
            super(msg);
        }
        public DBException(Exception e) {super(e); }

}


