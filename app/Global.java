import play.*;
import play.mvc.*;
import play.mvc.Http.*;
import play.libs.F.*;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookOAuthException;

import static play.mvc.Results.*;
import views.html.*;

public class Global extends GlobalSettings {

    public Promise<SimpleResult> onError(RequestHeader request, Throwable t) {
        return Promise.<SimpleResult>pure(internalServerError(
        		views.html.blank.render("Unexpected Error. Sorry!")
        ));
    }

    public Promise<SimpleResult> onHandlerNotFound(RequestHeader request) {
        return Promise.<SimpleResult>pure(notFound(
                views.html.blank.render("Action not found!")
        ));
    }

}
