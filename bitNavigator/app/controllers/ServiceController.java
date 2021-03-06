package controllers;


import models.Place;
import models.Service;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import utillities.Authenticators;
import views.html.*;

import views.html.service.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static views.html.service.editservice.*;

/**
 * This class represents Service controller
 */
public class ServiceController extends Controller {

    private static final Form<Service> serviceForm = Form.form(Service.class);

    /**
     *  This method is used to list all Services from database
     * @return - list of Services
     */
    @Security.Authenticated(Authenticators.Admin.class)
    public Result serviceList(){
        List<Service> services = Service.findAll();
        return ok(servicelist.render(services));
    }

    /**
     * This method delete selected Service from database
     * @param id - id of selected Service
     * @return
     */
    @Security.Authenticated(Authenticators.Admin.class)
    public Result delete(Integer id) {
        Service service = Service.findById(id);
        if (service == null) {
            return notFound(String.format("Service %s does not exists.", id));
        }

        service.delete();
        return redirect(routes.ServiceController.serviceList());
    }

    /**
     * This method edit selected Service
     * @param id - id of selected Service
     * @return renders edit service view
     */
    @Security.Authenticated(Authenticators.Admin.class)
    public Result editService(Integer id){
        Service service = Service.findById(id);
        if (service == null) {
            return notFound(String.format("Service %s does not exists.", id));
        }

        return ok(editservice.render(service));
        }

    /**
     * This method is used to update Service to database
     * @param id - id of selected Service
     * @return updates service
     */
    @Security.Authenticated(Authenticators.Admin.class)
    public Result updateService(Integer id) {

        Form<Service> boundForm = serviceForm.bindFromRequest();
        Service service = Service.findById(id);

        service.serviceType = boundForm.bindFromRequest().field("serviceType").value();
        service.serviceIcon = boundForm.bindFromRequest().field("serviceIcon").value();


        if (boundForm.bindFromRequest().field("isReservable").value().equals("on")){
            service.isReservable = true;
        } else {
            service.isReservable = false;
        }

        if(service.isReservable) {
            service.isReservable = true;
        } else {
            service.isReservable = false;
        }

        MultipartFormData body = request().body().asMultipartFormData();
        List<FilePart> picture = body.getFiles();

        if (boundForm.hasErrors()) {
            flash("error", "Enter service type and icon.");
            return badRequest(editservice.render(service));
        }

        if (!picture.isEmpty() && service.serviceType != null) {
            FilePart p = picture.get(0);
            File file = p.getFile();

            String imagepath = Play.application().path() + "/public/images/serviceImages/" + service.serviceType + ".png";

            try {

                FileUtils.moveFile(file, new File(imagepath));

                imagepath ="images/serviceImages/" + service.serviceType + ".png";
                service.serviceIcon = imagepath;

                service.update();
                return redirect(routes.ServiceController.serviceList());
            } catch (IOException ex) {
                Logger.info("Could not move file. " + ex.getMessage());
                flash("error", "Could not move file.");
            }
            return ok(index.render(Place.findAll()));
        } else {
            flash("error", "Enter service icon.");
            return badRequest(editservice.render(service));
        }
    }

    /**
     * This method is used to add new Service
     * @return
     */
    @Security.Authenticated(Authenticators.Admin.class)
    public Result addService() {
        return ok(addservice.render(serviceForm));
    }

    /**
     * This method save new Service to database
     * @return
     */
    @Security.Authenticated(Authenticators.Admin.class)
    public Result save() {

        Form<Service> boundForm = serviceForm.bindFromRequest();

        MultipartFormData body = request().body().asMultipartFormData();
        List<FilePart> picture = body.getFiles();

        if (boundForm.hasErrors()) {
            flash("error", "Enter service type an icon.");
            return badRequest(addservice.render(boundForm));
        }

        Service service = boundForm.get();

        if(service.isReservable) {
            service.isReservable = true;
        } else {
            service.isReservable = false;
        }

        if (!picture.isEmpty() && service.serviceType != null) {
            FilePart p = picture.get(0);
            File file = p.getFile();

            String imagepath = Play.application().path() + "/public/images/serviceImages/" + service.serviceType + ".png";

            try {

                FileUtils.moveFile(file, new File(imagepath));

                imagepath ="images/serviceImages/" + service.serviceType + ".png";
                service.serviceIcon = imagepath;

                service.save();
                return redirect(routes.ServiceController.serviceList());

            } catch (IOException ex) {
                Logger.info("Could not move file. " + ex.getMessage());
                flash("error", "Service already exist.");
                return badRequest(addservice.render(boundForm));
            }
        } else {
            flash("error", "Enter service icon.");
            return badRequest(addservice.render(boundForm));
        }
    }

}