package controllers;

import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import models.FacebookUser;
import models.Question;
import models.Score;
import models.User;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
import com.restfb.json.JsonObject;
import com.restfb.types.FriendList;

import play.*;
import play.libs.Json;
import play.mvc.*;
import services.UserService;
import utils.Constants;
import utils.Util;
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
    	
    	String screen_density = requestJson.findPath("screen_density").textValue();

    	
    	//Create mock FacebookUser object with info from Facebook.
    	FacebookUser facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
    	facebookUser.combine(facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class, 
    			Parameter.with("width", Util.getPictureSize(screen_density)), Parameter.with("redirect", false), 
    			Parameter.with("height", Util.getPictureSize(screen_density)), Parameter.with("type", "normal")));
    	
    	User tempUser = new User();
    	tempUser.mapFacebookUser(facebookUser);
    	User user = authentication(tempUser);
    	
    	Score score = new Score();
		try {
			score = UserService.retrieveScore(user);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if(user != null) {
	    	//Return JSON.
	    	ObjectNode result = Json.newObject();
	    	result.put("id", user.get_id());
	    	result.put("name", user.getName());
	    	result.put("questions", user.getQuestions().toString());
	    	result.put("date_created", user.getDateCreated());
	    	result.put("photo_url", facebookUser.getData().getUrl());
	    	result.put("score", score.getScore());
	    	
	    	status = ok(result);
    	} else {
    		status = unauthorized();
    	}
    	return status;
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateQuestions() {

		String questionKey = null;
		String questionsString = null;
		
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
    	
    	String resultString = userQuestions.toString();
    	resultString = resultString.replace(":\"[", ": [");
    	resultString = resultString.replace("]\"", "]");
    	resultString =  resultString.replace("\\\"", "\"");
    	StringBuilder rS = new StringBuilder(resultString);
    	rS.delete(0, 1);	
        
        questionsString = rS.toString();
		Object o = com.mongodb.util.JSON.parse(questionsString);
		BasicDBList dbObj = (BasicDBList) o;

    	updatedUser.setQuestions(dbObj);
    
    	try {
    		UserService.updateQuestions(updatedUser);
    		
    	} catch (Exception e) {
    	
    		questions.add("error");
    		PrintStream s = null;
    		e.printStackTrace();
    		//questions.add(s);
    	}
    	questions.add(updatedUser.getQuestions() + "");
    	
    	Status status = null;

		String screen_density = requestJson.findPath("screen_density").textValue();
    	
    	facebookUser.combine(facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class, 
    			Parameter.with("width", Util.getPictureSize(screen_density)), Parameter.with("redirect", false), 
    			Parameter.with("height", Util.getPictureSize(screen_density)), Parameter.with("type", "normal")));
    	
    	User tempUser = new User();
    	tempUser.mapFacebookUser(facebookUser);
    	User user = authentication(tempUser);
    	
    
    	
    	Score score = new Score();
		try {
			score = UserService.retrieveScore(user);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	if(user != null) {
	    	//Return JSON.
	    	ObjectNode result = Json.newObject();
	    	result.put("id", user.get_id());
	    	result.put("name", user.getName());
	    	result.put("questions", questionsString);
	    	result.put("date_created", user.getDateCreated());
	    	result.put("photo_url", facebookUser.getData().getUrl());
	    	result.put("score", score.getScore());
	    	
	    	status = ok(result);
    	} else {
    		status = unauthorized();
    	}
    	return status;
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result loadQuestions() {
    	
       	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	FacebookUser facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
    	User updatedUser = new User();
    	updatedUser.mapFacebookUser(facebookUser);
    
    	JsonNode userQuestions = requestJson.findPath("current_end_index");
    	int index = Integer.parseInt(userQuestions.textValue());
    	Status status = null;
    	
    	try {
    		Iterable<Question> questions = UserService.loadQuestionsFromIndex(index);
    		ArrayList<String> questionList = new ArrayList<String>();
    		Iterator<Question> iter = questions.iterator();
    		while(iter.hasNext()) {
    			Question next = iter.next();
    			questionList.add(next.getQuestion());
    		}
    		BasicDBList newList = new BasicDBList();
    		for(String q: questionList) {
    			BasicDBObject obj = new BasicDBObject(1);
    			obj.put("question", q);
    			newList.add(obj);
    		}

    		ObjectNode result = Json.newObject();
	    	result.put("questions", newList.toString());

	    	status = ok(result);
    		
    	} catch (Exception e) {
    		status = unauthorized();
    		e.printStackTrace();
    	}
    	return status;
    	
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateScore() {
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	FacebookUser facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
    	User user = new User();
    	user.mapFacebookUser(facebookUser);
    	
    	JsonNode userQuestions = requestJson.findPath("new_score");
    	int score_value = Integer.parseInt(userQuestions.textValue());
    	
    	Score score = new Score();
    	score.set_id(user.get_id());
    	score.setScore(score_value + "");
    	
    	try {
			UserService.updateScore(score);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
    	
    	Status status = null;
    	
    	try {

    		status = ok();
    	} catch (Exception e) {
    		status = unauthorized();
    		e.printStackTrace();
    	}
    	return status;
    	
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result loadLeaderboard() {
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	FacebookUser facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
    	User user = new User();
    	user.mapFacebookUser(facebookUser);
    	
    	
    	com.restfb.Connection<JsonObject> myFriends = facebookClient.fetchConnection("me/friends", JsonObject.class, Parameter.with("fields", "picture,name"));
    	
    	Status status = null;
    
    	ObjectNode parent = Json.newObject();
    	
    	ObjectNode child = Json.newObject();

    	
    	ArrayNode apps = child.putArray("leaderboard");
    	
    	for(JsonObject u: myFriends.getData()) {
    		ObjectNode uNode = Json.newObject();
    		uNode.put("id", u.getString("id"));
    		uNode.put("rank", "1");
    		uNode.put("name", u.getString("name"));
    	
    		uNode.put("photo_url", u.getJsonObject("picture").getJsonObject("data").getString("url"));
    		uNode.put("score", "999");
    		
    		apps.add(uNode);
    	}
    	
    	try {
    		status = ok(child);
    	} catch (Exception e) {
    		status = unauthorized();
    	}
    	return status;
    }
    
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result fetchPlayer() {
    	
    	//long start = System.nanoTime();
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
    	
    	String PLAYER_INDEX = requestJson.findPath("player_index").textValue();
    	int int_player_index = 0;
    	try {
    		int_player_index = Integer.parseInt(PLAYER_INDEX);
    	} catch(NumberFormatException e) {
    		e.printStackTrace();
    	}
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	com.restfb.Connection<JsonObject> myFriends = facebookClient.fetchConnection("me/friends", JsonObject.class, Parameter.with("fields", "picture.height(400),name"));
    	List<JsonObject> friends_list = myFriends.getData(); 	
    	
    	//Query DB for friends with ID
    	User user_quizter_friend = null;
    	try {
			user_quizter_friend = UserService.getPlayerForFriend(friends_list, int_player_index);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
    	
		
    	FacebookUser user_with_picture = facebookClient.fetchObject(user_quizter_friend.get_id() + "/picture", FacebookUser.class, 
    			Parameter.with("width", 400), Parameter.with("redirect", false), 
    			Parameter.with("height", 400), Parameter.with("type", "normal"));
    	ObjectNode result = Json.newObject();
    	result.put("available_players", "false");	
    	if(user_quizter_friend != null) {
    		result.put("_id", user_quizter_friend.get_id());
        	result.put("name", user_quizter_friend.getName());
        	result.put("photo_url", user_with_picture.getData().getUrl());
        	result.put("available_players", "true");	
     	}


    	Status status = null;
    	try{
    		status = ok(result);
    	} catch (Exception e) {
    		status = unauthorized();
    	}
    	
    	return status;
    	
    }
    
}
