package person.fx;

public class SessionParameters {
    static String sessionToken;
    static int id;

    public static String getSessionToken(){return sessionToken;}

    public static void setSessionToken(String sessionId){

        SessionParameters.sessionToken = sessionId;
    }

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        SessionParameters.id = id;
    }
}
