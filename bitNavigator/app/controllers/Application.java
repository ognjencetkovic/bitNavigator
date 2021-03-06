package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

import models.*;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import utillities.SessionHelper;
import views.html.aboutus;
import views.html.index;
import play.libs.F;
import play.libs.ws.*;

import views.html.user.*;

import java.util.*;
import it.innove.play.pdf.PdfGenerator;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class Application extends Controller {


    public Result showFaq() {
        List<FAQ> faqs = FAQ.findAll();
        return ok(faq.render(faqs));
    }

    public Result index() {
        DynamicForm form = Form.form().bindFromRequest();
        String srchTerm = form.data().get("srch-term");
        List<Place> places = Place.findAll();
        if(srchTerm != null) {
            places = Place.findByValue(srchTerm);
        }
        return ok(index.render(places));
    }

    public Result paypal(Integer id) {
        try {
            Reservation r = Reservation.findById(id);
            // Configuration
            String clientId = Play.application().configuration().getString("clientId");
            String secret = Play.application().configuration().getString("secret");

            String token = new OAuthTokenCredential(clientId, secret).getAccessToken();

            Map<String, String> config = new HashMap<>();
            config.put("mode", "sandbox");

            APIContext context = new APIContext(token);
            context.setConfigurationMap(config);

            // Process cart/payment information
            double price = r.price;
            String priceString = String.format("%1.2f", price);
            String desc = "Reserved: " + r.place.title + "\n\n Amount: "+priceString+" BAM \n\n Reservation date: "+ r.reservationDate.getTime();

            // Configure payment
            Amount amount = new Amount();
            amount.setTotal(priceString);
            amount.setCurrency("USD");

            List<Transaction> transactionList = new ArrayList<>();
            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setDescription(desc);
            transactionList.add(transaction);

            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            Payment payment = new Payment();
            payment.setPayer(payer);
            payment.setIntent("sale");
            payment.setTransactions(transactionList);

            RedirectUrls redirects = new RedirectUrls();
            redirects.setCancelUrl("http://localhost:9000/");
            redirects.setReturnUrl("http://localhost:9000/paypal/success");
            payment.setRedirectUrls(redirects);

            Payment madePayments = payment.create(context);
            r.paymentId = madePayments.getId();
            r.update();
            Iterator<Links> it = madePayments.getLinks().iterator();
            while(it.hasNext()) {
                Links link = it.next();
                if(link.getRel().equals("approval_url")) {
                    System.out.println("Link: " + link.getHref());
                    return redirect(link.getHref());
                }
            }
        } catch(PayPalRESTException e){
            Logger.warn("PayPal Exception");
            e.printStackTrace();
        }

        return redirect("/");
    }

    public Result paypalSuccess(){
        APIContext context;
        PaymentExecution paymentExecution;
        Payment payment;
        DynamicForm form = Form.form().bindFromRequest();
        String paymentId = form.data().get("paymentId");
        String payerID = form.data().get("PayerID");
        String clientId = Play.application().configuration().getString("clientId");
        String secret = Play.application().configuration().getString("secret");
        try {
            String accessToken = new OAuthTokenCredential(clientId,
                    secret).getAccessToken();
            Map<String, String> sdkConfig = new HashMap<String, String>();
            sdkConfig.put("mode", "sandbox");
            context = new APIContext(accessToken);
            context.setConfigurationMap(sdkConfig);
            payment = Payment.get(accessToken, paymentId);
            paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerID);

            //Executes a payment
            Payment newPayment = payment.execute(context, paymentExecution);


            flash("info");
        } catch (Exception e) {
            flash("error");
            Logger.debug("Error at purchaseSucess: " + e.getMessage(), e);
            return redirect("/");
        }

        Reservation r = Reservation.findByPaymentId(paymentId);
        models.Status status = models.Status.findById(models.Status.APPROVED);
        r.status = status;
        r.update();

        Message message = new Message();
        message.sender = SessionHelper.getCurrentUser();
        message.reciever = r.place.user;
        String msg = "Transaction successful!" + "\n Transaction ID: " + r.paymentId;
        message.content = msg;
        message.reservation = r;
        message.sent = Calendar.getInstance();
        message.save();

        return ok(paypal.render(r));
    }

    @Inject
    public PdfGenerator pdfGenerator;

    public  Result payPdf(Integer id){
        Reservation r = Reservation.findById(id);
        return pdfGenerator.ok(paypdf.render(r), "http://localhost:9000");
    }
    
    @Inject
    WSClient ws;

    public Result getHotels() {
        WSRequest rq = ws.url("http://rockit.cloudapp.net/api/hotelsForNavigator");

        F.Promise<JsonNode> responsePromise = rq.get().map(response -> {
            return response.asJson();
        });

        JsonNode json = responsePromise.get(3000);

        return ok(json.toString());
    }

    public Result getPostOffices() {
        WSRequest rq = ws.url("http://10.0.82.18:9000/postofficelist/a1b2c3d4e5");

        F.Promise<JsonNode> responsePromise = rq.get().map(response -> {
            return response.asJson();
        });

        JsonNode json = responsePromise.get(3000);

        return ok(json.toString());
    }

    public Result aboutUs() {
        return ok(aboutus.render());
    }

}
