package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.ArrayList;

/**
 * Created by Thomas on 4/2/2014.
 */
public class WebController extends Controller {

    public static Result index() {
        return ok(views.html.index.render());
    }
}
