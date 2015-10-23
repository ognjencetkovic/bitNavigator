package controllers;

import models.*;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utillities.Authenticators;
import utillities.SessionHelper;
import views.html.reservations.reservationlist;

import javax.persistence.PersistenceException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by amra.sabic on 9/17/2015.
 */
public class ReservationController extends Controller {

    private static final Form<Reservation> reservationForm = Form.form(Reservation.class);

    @Security.Authenticated(Authenticators.User.class)
    public Result submitReservation(int id) {
        DynamicForm boundForm = Form.form().bindFromRequest();
        if (boundForm.hasErrors()) {
            return badRequest("dsadas");
        }

        User user = User.findByEmail(session().get("email"));

        Place place = Place.findById(id);

        String content = boundForm.data().get("content");


        String reservationDay = boundForm.data().get("reservationDay");
        String reservationtime = boundForm.data().get("reservationTime");
        SimpleDateFormat myDate = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        Calendar date = new GregorianCalendar();
        try {
            date.setTime(myDate.parse(reservationDay + " " + reservationtime));
        } catch (ParseException e) {
            return badRequest("qwe");
        }

        Reservation r = new Reservation();
        r.place = place;
        r.user = user;

        Message message = new Message();
        message.sender = SessionHelper.getCurrentUser();
        if (user.equals(r.place.user)) {
            message.reciever = r.user;
        } else {
            message.reciever = r.place.user;
        }
        if (content == null) {
            return TODO;
        } else {
            message.content = content;
        }

        r.timestamp = Calendar.getInstance();
        r.reservationDate = date;
        r.messages.add(message);
        r.status = models.Status.findById(models.Status.WAITING);
        r.save();
        message.sent = Calendar.getInstance();
        message.reservation.id = r.id;
        message.save();
        if (place.numOfReservations == null) {
            place.numOfReservations = 0;
        }
        place.numOfReservations++;
        place.update();
        return redirect(routes.Application.index());
    }

    @Security.Authenticated(Authenticators.User.class)
    public Result reservationsList() {
        return ok(reservationlist.render(Reservation.findAll(), models.Status.findAll()));
    }

    @Security.Authenticated(Authenticators.User.class)
    public Result changeStatus() {
        DynamicForm boundForm = Form.form().bindFromRequest();
        models.Status status = models.Status.findById(Integer.parseInt(boundForm.data().get("statusId")));
        Reservation reservation = Reservation.findById(Integer.parseInt(boundForm.data().get("reservationId")));
        // Logger.info(reservation.title);
        if (status == null || reservation == null) {
            return badRequest("Something went wrong");
        }
        reservation.status = status;
        reservation.update();

        return redirect(routes.ReservationController.reservationsList());
    }

    @Security.Authenticated(Authenticators.User.class)
    public Result setPrice() {
        DynamicForm boundForm = Form.form().bindFromRequest();
        Reservation r = Reservation.findById(Integer.parseInt(boundForm.data().get("reservationId")));
        List<Message> messages = Message.findByReservation(Integer.parseInt(boundForm.data().get("reservationId")));
        String prc = boundForm.data().get("priceId");
        Double price = Double.parseDouble(prc);
        r.price = price;
        r.update();
        Message message = new Message();
        message.sender = SessionHelper.getCurrentUser();
        message.reciever = r.user;
        String msg = "To confirm reservation at " + r.place.title + ", please transfer us " + price + " KM.";
        message.content = msg;
        message.reservation = r;
        message.sent = Calendar.getInstance();
        message.save();
        messages.add(message);
        return redirect(routes.ReservationController.reservationsList());
    }

    public Result validateForm() {
        //get the form data from the request - do this only once
        Form<Message> binded = Form.form(Message.class).bindFromRequest();
        DynamicForm boundForm = Form.form().bindFromRequest();
        String reservationDay = boundForm.data().get("reservationDay");
        String reservationtime = boundForm.data().get("reservationTime");
        SimpleDateFormat myDate = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        Calendar date = new GregorianCalendar();
        try {
            date.setTime(myDate.parse(reservationDay + " " + reservationtime));
        } catch (ParseException e) {
            return badRequest("Must choose date and time!");
        }
        //if we have errors just return a bad request
        if (binded.hasErrors()) {
            return badRequest(binded.errorsAsJson());
        } else {
            //get the object from the form, for revere take a look at someForm.fill(myObject)
            return redirect("/");
        }

    }

    @Security.Authenticated(Authenticators.User.class)
    public Result delete(Integer id) {
        Reservation reservation = Reservation.findById(id);
        if (reservation.status.id == models.Status.WAITING) {
            for (Message message : Message.findByReservation(id)) {
                message.delete();
            }
            reservation.delete();
        }

        return redirect(routes.ReservationController.reservationsList());
    }

    @Security.Authenticated(Authenticators.User.class)
    public Result getWorkingHours() {
        DynamicForm form = Form.form().bindFromRequest();
        Place place = Place.findById(Integer.parseInt(form.get("placeId")));
        int dayOfWeek = Integer.parseInt(form.get("dayOfWeek"));
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        String response = "";

        Integer openingTime = WorkingHours.getOpeningTime(place, dayOfWeek);
        Integer closingTime = WorkingHours.getClosingTime(place, dayOfWeek);
        if (openingTime != null || closingTime != null) {
            response = String.format("%02d:%02d:00-%02d:%02d:00", openingTime / 60, openingTime % 60, closingTime / 60, closingTime % 60);
        } else {
            response = "not working";
        }

        return ok(response);
    }

    public Result updateReservation(Integer id) {

        Form<Reservation> boundForm = reservationForm.bindFromRequest();

        Reservation reservation = Reservation.findById(id);

//        reservation.title = boundForm.bindFromRequest().field("rtitle").value();
//        reservation.description = boundForm.bindFromRequest().field("rdescription").value();

//        Logger.info(reservation.description);
//        Logger.info(reservation.title);
        reservation.update();
        return redirect(routes.ReservationController.reservationsList());
    }

    public static void checkReservationExpiration() {
        models.Status status = models.Status.findById(models.Status.WAITING);
        models.Status status1 = models.Status.findById(models.Status.DENIED);
        List<Reservation> reservations = Reservation.finder.where().eq("status", status).findList();

        final Date currentDate = new Date();

        for (Reservation reservation : reservations) {
            if (reservation.price != null) {
                Date reservationCheckOutDate = new Date(reservation.timestamp.getTimeInMillis());
                Long res = currentDate.getTime() - reservationCheckOutDate.getTime();
                if (res >= 172800000) {
                    reservation.status = status1;
                    try {
                        reservation.update();
                    } catch (PersistenceException e) {
                        Logger.info("Could not update reservation.");
                        Logger.error("Error message " + e.getMessage());
                    }
                }
            }
        }
    }


}
