package controllers;

import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import models.FacebookUser;
import models.User;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
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
    
    public static User authentication(User newUser) {
    	User user = null;
    	try {
    		user = UserService.userAlreadyExists(newUser);
			if(user == null) {
				user = UserService.registerUser(newUser);
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
	    	StringBuilder questions = new StringBuilder(user.getQuestions().toString());
	    	questions = questions.delete(0, 1);
	    	questions = questions.delete(questions.length() - 1, questions.length());
	    	String questionsString = questions.toString();
	    	result.put("questions", questionsString);
	    	result.put("date_created", user.getDateCreated());
	    	result.put("photo_url", facebookUser.getData().getUrl());
	    	
	    	status = ok(result);
    	} else {
    		status = unauthorized();
    	}
    	return status;
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateQuestions() {
    	
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	FacebookUser facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
    	User updatedUser = new User();
    	updatedUser.mapFacebookUser(facebookUser);
    
    	JsonNode userQuestions = requestJson.findPath("updated_questions");
    	
    	List<String> questions = new ArrayList<String>();
    	questions.add(userQuestions.elements() + "");
    	
    	BasicDBList mongoQuestions = new BasicDBList();
  
    	Iterator<JsonNode> iter = userQuestions.elements();
    	while (iter.hasNext()) {
    		ArrayList<BasicDBObject> objects = new ArrayList<BasicDBObject>();
    		
			JsonNode node = iter.next();
			
			String questionKey = node.path("question").textValue();
			String questionValue = node.path("answer").textValue();
			
			questionKey.replace("\"", "");
			questionValue.replace("\"", "'");

			BasicDBObject newObj = new BasicDBObject(2);
			newObj.put("question", questionKey);
			newObj.put("answer", questionValue);

			mongoQuestions.add(newObj);
			
		}
    	
    	//BasicDBObject obj = new BasicDBObject("questions", mongoQuestions);
    	updatedUser.setQuestions(mongoQuestions);
    	
    	try {
    		UserService.updateQuestions(updatedUser);
    		
    	} catch (Exception e) {
    	
    		questions.add("error");
    		PrintStream s = null;
    		e.printStackTrace();
    		//questions.add(s);
    		return ok(views.html.questions.render(questions));
    	}
    	questions.add(updatedUser.getQuestions() + "");
    	
    	return ok(views.html.questions.render(questions));
    }
}
