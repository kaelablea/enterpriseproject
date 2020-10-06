package person;

//Should probably make some nice exceptions......
public class PersonException extends RuntimeException {
    public PersonException(Exception e) {
        super(e);
    }

    public PersonException(String msg) {
        super(msg);
    }
}

