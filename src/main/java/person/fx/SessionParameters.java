package person.fx;

public class SessionParameters {
    static String sessionToken;

    public static String getSessionToken(){return sessionToken;}

    public static void setSessionToken(String sessionId){
        SessionParameters.sessionToken = sessionId;
    }
}
