package controllers;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import models.FacebookUser;
import models.User;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;

import play.*;
import play.libs.Json;
import play.mvc.*;
import services.UserService;
import utils.Constants;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render(new ArrayList<String>()));
    }
    
    public static boolean authentication(User user) {
    	boolean authenticated = true;
    	try {
    		
			if(!UserService.userAlreadyExists(user)) {
				UserService.registerUser(user);
			}

    	} catch (UnknownHostException e) {
    		authenticated = false;
			e.printStackTrace();
		}
    	
    	return authenticated;
    }
        
    @BodyParser.Of(BodyParser.Json.class)
    public static Result connectUser() {
    	
    	Status status = null;
    	
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	//Create mock FacebookUser object with info from Facebook.
    	FacebookUser facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
    	facebookUser.combine(facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class, 
    			Parameter.with("width", 200), Parameter.with("redirect", false), 
    			Parameter.with("height", 200), Parameter.with("type", "normal")));
    	
    	User user = new User();
    	user.mapFacebookUser(facebookUser);
    	boolean verified = authentication(user);

    	if(verified) {
	    	//Return JSON.
	    	ObjectNode result = Json.newObject();
	    	result.put("id", facebookUser.getId());
	    	result.put("name", facebookUser.getName());
	    	result.put("photo_url", facebookUser.getData().getUrl());
	    	
	    	status = ok(result);
    	} else {
    		status = unauthorized();
    	}
    	return status;
    }
}
