package utillities;

import com.avaje.ebean.Ebean;
import models.*;
import play.mvc.Http;

public class SessionHelper {

    //Returns user from session
    private static User getUserFromSession() {
        Http.Context ctx = Http.Context.current();
        String email = ctx.session().get("email");
        if (email != null && !email.isEmpty()) {
            User user = User.findByEmail(email);
            return user;
        }
        return null;
    }

    //Gets current user from session
    public static User getCurrentUser() {
        return getUserFromSession();
    }

    //Gets current users email
    public static String getCurrentUsersEmail() {
        User user = getUserFromSession();
        if (user == null)
            return "Anonymous";
        return user.email;
    }

    //Checks if user is admin
    public static boolean isAdmin() {
        User user = getUserFromSession();
        if (user == null)
            return false;
        return user.isAdmin();
    }

    //Checks if user is authenticated
    public static boolean isAuthenticated() {
        return getUserFromSession() != null;
    }

    //Checks number of notifications by new reservations and messages that is not seen
    public static int numberOfNotifications() {
        return Reservation.getAllReservationsOnUsersPlaces(Status.findById(Status.WAITING)).size() + Message.getNewMessages(getCurrentUser()).size();
    }

    //Checks if user has verified phone number
    public static boolean hasValidatedPhoneNumber(){
        for (PhoneNumber phoneNumber : PhoneNumber.findByUser(getCurrentUser())){
            if(phoneNumber.isValidated()){
                return true;
            }
        }
        return false;
    }


    public static boolean checkIp(Place place, String clientIP){
        //String clientIP = request().remoteAddress();
        for(int i = 0; i<place.clientIPs.size(); i++){
            if(place.clientIPs.get(i).ipAddress.equals(clientIP)){
                return false;
            }
        }

        ClientIP ipAddress = new ClientIP();
        ipAddress.ipAddress = clientIP;
        ipAddress.place=place;
        Ebean.save(ipAddress);
        place.clientIPs.add(ipAddress);
        place.update();
        return true;
    }

    public static boolean isOwner(Place place) {
        if (isAuthenticated()) {
            return place.user.equals(getCurrentUser());
        }
        return false;
    }

}