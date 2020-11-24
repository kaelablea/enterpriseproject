package person.backend.repocontrollers;


public class DBException extends RuntimeException {
    public DBException(String msg) {
            super(msg);
        }
        public DBException(Exception e) {super(e); }

}


