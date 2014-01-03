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
    
    public static User authentication(User user) {
    	try {
    		User user = UserService.userAlreadyExists(user);
			if(user == null) {
				user = UserService.registerUser(user);
			}

    	} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    	
    	return user;
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
    	
    	User tempUser = new User();
    	tempUser.mapFacebookUser(facebookUser);
    	User user = authentication(tempUser);

    	if(user != null) {
	    	//Return JSON.
	    	ObjectNode result = Json.newObject();
	    	result.put("id", user.get_id());
	    	result.put("name", user.getName());
	    	result.put("date_created", user.getDateCreated());
	    	result.put("photo_url", facebookUser.getData().getUrl());
	    	
	    	status = ok(result);
    	} else {
    		status = unauthorized();
    	}
    	return status;
    }
}
