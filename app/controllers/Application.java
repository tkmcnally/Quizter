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
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result register() {
    	
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	 
    	//Check for required fields
    	String email = requestJson.findPath("email").textValue();
    	String firstName = requestJson.findPath("firstName").textValue();
    	String lastName = requestJson.findPath("lastName").textValue();
    	String password =  requestJson.findPath("password").textValue();
    	
    	User newUser = new User();
    	newUser.setEmail(email);
    	newUser.setFirstName(firstName);
    	newUser.setLastName(lastName);
    	newUser.setPassword(password);
    	
    	//Check if user can register
    	Status status = null;
    	ObjectNode result = Json.newObject();
    	try {
			if(UserService.userAlreadyExists(email)) {
				result.put("message", email + " is already registered!");
				status = badRequest(result);
				
			} else {
				result.put("message", email + " has been registered!");
				status = ok(result);
				UserService.registerUser(newUser);
				
			}
		} catch (UnknownHostException e) {
			status = badRequest("Connection to database could not be made!");
			e.printStackTrace();
		}
    	
    	//Return status
    	return status;	
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result authenticate() {

    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String email = requestJson.findPath("email").textValue();
    	String password =  requestJson.findPath("password").textValue();
    	
    	User user = new User();
    	user.setEmail(email);
    	user.setPassword(password);
    	
    	Status status = null;
    	ObjectNode result = Json.newObject();
    	try {
			if(UserService.authenticateUser(user)) {
				result.put("message", email + " has successfully logged in!");
				status = ok(result);
				
			} else {
				result.put("message", email + " has not registered.");
				status = badRequest(result);
				
			}
		} catch (UnknownHostException e) {
			status = badRequest("Connection to database could not be made!");
			e.printStackTrace();
		}
    	
    	return status;
    }	
    
    public static Result htmlFacebookTest(String ACCESS_TOKEN) {
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	//Facebook user  
    	FacebookUser facebookUser1 = facebookClient.fetchObject("me", FacebookUser.class);
    	
    	FacebookUser facebookUser2 = facebookClient.fetchObject(facebookUser1.getId() + "/picture", FacebookUser.class, 
    			Parameter.with("width", 200), Parameter.with("redirect", false), 
    			Parameter.with("height", 200), Parameter.with("type", "normal"));
    	
    	facebookUser1.setData(facebookUser2.getData());
    	
    	List<String> information = new ArrayList<String>();
    	information.add("" + facebookUser1.getId());
    	information.add(facebookUser1.getData().getUrl() + "");
    	
    	
    	return ok(views.html.facebooktest.render(information));
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result androidFacebookTest() {
    	Status status = null;
    	
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	//Facebook user  
    	FacebookUser facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
    	
    	FacebookUser facebookUser2 = facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class, 
    			Parameter.with("width", 200), Parameter.with("redirect", false), 
    			Parameter.with("height", 200), Parameter.with("type", "normal"));
    	
    	facebookUser.setData(facebookUser2.getData());
    	
    	ObjectNode result = Json.newObject();
    	
    	result.put("id", facebookUser.getId());
    	result.put("photo_url", facebookUser.getData().getUrl());
    	
    	status = ok(result);
    	
    	return status;
    }
}
