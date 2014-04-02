package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by Thomas on 4/2/2014.
 */
public class WebController extends Controller {

    // Default index page.
    public static Result index() {
        return ok(views.html.index.render());
    }
}
