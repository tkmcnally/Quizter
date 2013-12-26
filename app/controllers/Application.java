package controllers;

import java.net.UnknownHostException;

import models.User;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import play.*;
import play.libs.Json;
import play.mvc.*;
import services.UserService;
import utils.Constants;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Cool."));
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
    	String returnMessage;
    	Status status = null;
    	try {
			if(UserService.userAlreadyExists(email)) {
				returnMessage = "That username is already registered!";
				status = badRequest(returnMessage);
				
			} else {
				returnMessage = email + " has been registered!";
				status = ok(returnMessage);
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
    

}
