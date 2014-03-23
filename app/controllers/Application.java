package controllers;

import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import models.FacebookUser;
import models.Question;
import models.Score;
import models.User;
import models.UserAnsweredQuestions;

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
import services.ScoreComparator;
import services.UserService;
import utils.Constants;
import utils.Util;
import utils.WordMatcher;
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
    	
    	if(user != null) {
	    	//Return JSON.
	    	ObjectNode result = Json.newObject();
	    	result.put("id", user.get_id());
	    	result.put("name", user.getName());
	    	result.put("questions", user.getQuestions().toString());
	    	result.put("date_created", user.getDateCreated());
	    	result.put("photo_url", facebookUser.getData().getUrl());
	    	result.put("score", user.getScore());
	    	
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
        
    	try {
    
	        questionsString = rS.toString();
	        
	        
	     
			Object o = com.mongodb.util.JSON.parse(questionsString);
			System.out.println(o);
			BasicDBList obj = (BasicDBList) o;
			   System.out.println("BasicDBList = " + obj.toString());
		        
	     
	    	updatedUser.setQuestions(Util.hashQuestions(obj));
    	} catch (Exception e) {
    		e.printStackTrace();
    		Status status = null;
    		return (status = ok(e.toString() + " " + e.getMessage() + " " + e.getStackTrace().toString() + " "+ "\n" + questionsString));
    	}
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
    	
    
    
		
    	if(user != null) {
	    	//Return JSON.
	    	ObjectNode result = Json.newObject();
	    	result.put("id", user.get_id());
	    	result.put("name", user.getName());
	    	result.put("questions", updatedUser.getQuestions().toString());
	    	result.put("date_created", user.getDateCreated());
	    	result.put("photo_url", facebookUser.getData().getUrl());
	    	result.put("score", user.getScore());
	    	
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
    	
    	
		HashMap<String, HashMap<String, String>> friends_as_users = new HashMap<String, HashMap<String, String>>();	
    	for(JsonObject u: myFriends.getData()) {
    		   		
    		HashMap<String, String> map1 = new HashMap<String, String>();
    		map1.put("photo", u.getJsonObject("picture").getJsonObject("data").getString("url"));
    		map1.put("name", u.getString("name"));
    		
    		friends_as_users.put(u.getString("id"), map1);
    	}
    	
    	Iterable<User> db_users = null;
		try {
			db_users = UserService.getFriendsOfPlayer(myFriends.getData());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		if(db_users != null) {
	    	List<User> users = (List<User>) makeCollection(db_users);
	    	Collections.sort(users, new ScoreComparator());
	    	for(int i = 0; i < users.size(); i++) {
	    		
	    		User temp_user = users.get(i);
	    		
	    		ObjectNode uNode = Json.newObject();
	    		uNode.put("id", user.get_id());
	    		uNode.put("rank", i + 1);
	    		uNode.put("name", friends_as_users.get(temp_user.get_id()).get("name")); 	
	    		uNode.put("photo_url", friends_as_users.get(temp_user.get_id()).get("photo"));
	    		uNode.put("score", temp_user.getScore());
	    		apps.add(uNode);
	    	}
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
    		result.put("id", user_quizter_friend.get_id());
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
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getQuestionsForQuiz() {
    	
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String PLAYER_ID = requestJson.findPath("player_id").textValue();
    	
    	//Create temporary user with ID from JSON
    	User tempUser = new User();
    	tempUser.set_id(PLAYER_ID);
    	
    	//Retrieve User from database
    	User PLAYER = null;
    	try {
			PLAYER = UserService.userAlreadyExists(tempUser);	
    	} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
    	
    	//Construct question list
		BasicDBList questionDBList = PLAYER.getQuestions();
		BasicDBList filtered_questions = new BasicDBList();
		for(Object obj: questionDBList) {
			LinkedHashMap qa = (LinkedHashMap) obj;
			qa.remove("answer");
			filtered_questions.add(qa);
		}

		
		//Construct JSON to return
    	ObjectNode result = Json.newObject();
    	result.put("questions", filtered_questions.toString());
    	result.put("player_id", PLAYER_ID);
    

    	Status status = null;
    	try{
    		status = ok(result);
    	} catch (Exception e) {
    		status = unauthorized();
    	}
    	
    	
    	return status;
    }
    

    @BodyParser.Of(BodyParser.Json.class)
    public static Result submitQuiz() {
    	

		String questionKey = null;
		String questionsString = null;
		
    	//JSON object from request.
    	JsonNode requestJson = request().body().asJson();
    	
    	//Check for un/pw
    	String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();

    	String PLAYER_ID = requestJson.findPath("player_id").textValue();
    	
    	String screen_density = requestJson.findPath("screen_density").textValue();

    	
    	
    	//Public facebook client accessor
    	FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
    	
    	FacebookUser facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
    	User current_user = new User();
    	current_user.mapFacebookUser(facebookUser);
    	
    	FacebookUser friend = facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class, 
    			Parameter.with("width", Util.getPictureSize(screen_density)), Parameter.with("redirect", false), 
    			Parameter.with("height", Util.getPictureSize(screen_density)), Parameter.with("type", "normal"));
  
    	JsonNode userQuestions = requestJson.findPath("question_answer");
    	
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

		
		User updatedUser = new User();
    	updatedUser.setQuestions(dbObj);
    	

    	//Create temporary user with ID from JSON
    	User tempUser = new User();
    	tempUser.set_id(PLAYER_ID);
    	
    	User quiz_player = null;
		try {
			quiz_player = UserService.userAlreadyExists(tempUser);
			current_user = UserService.userAlreadyExists(current_user);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	List<String> old_questions = new ArrayList<String>();
    	List<String> old_answers = new ArrayList<String>();
    	for(Object obj: updatedUser.getQuestions()) {
    		LinkedHashMap lhm = (LinkedHashMap) obj;
    		old_questions.add((String) lhm.get("question"));
    		old_answers.add((String) lhm.get("answer"));
    	}
    	
    	List<String> new_questions = new ArrayList<String>();
    	List<String> new_answers = new ArrayList<String>();
    	for(Object obj: quiz_player.getQuestions()) {
    		LinkedHashMap lhm = (LinkedHashMap) obj;
    		new_questions.add((String) lhm.get("question"));
    		new_answers.add((String) lhm.get("answer"));
    	}

    	
    	//GET ANSWERED QUESTIONS
    	UserAnsweredQuestions questionsAnswered = null;
    	try {
			questionsAnswered = UserService.getQuestionsAnswered(current_user);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	ObjectNode result = Json.newObject();
    	if(new_questions.containsAll(old_questions)) {
    		result.put("valid", true);
    	
	    	BasicDBList question_overview = new BasicDBList();
	    	int score = 0;
	    	for(int i = 0; i < old_answers.size(); i++) {
	    		BasicDBObject obj = new BasicDBObject(3);
	    		obj.put("question", old_questions.get(i));
    			obj.put("given_answer", old_answers.get(i));
	    		if(!WordMatcher.doesMatch(old_answers.get(i), new_answers.get(i))) {
	    			obj.put("correct_answer", "false");  			
	    		} else {
	    			String hash = (new_answers.get(i) + new_questions.get(i)).hashCode() + "";
	    			System.out.println(questionsAnswered.getQuestionsAnswered() );
	    			if(questionsAnswered == null || questionsAnswered.getQuestionsAnswered() == null || questionsAnswered.getQuestionsAnswered().isEmpty()) {
	    				obj.put("already_answered", "false");
	    				questionsAnswered.setQuestionsAnswered(new BasicDBList());
	    				questionsAnswered.getQuestionsAnswered().add(hash);
	    				score++;
	    			} else if(!questionsAnswered.getQuestionsAnswered().contains(hash)) {
	    				obj.put("already_answered", "false");
	    				questionsAnswered.getQuestionsAnswered().add(hash);
	    				score++;
	    			} else {			
	    				obj.put("already_answered", "true");
	    			}
	    			obj.put("correct_answer", "true");
	    		}
	    		question_overview.add(obj);
	    	}
	    	result.put("score", score);
	    	result.put("marked_questions", question_overview.toString());
	    	result.put("photo_url", friend.getData().getUrl());
    	
	    	
	    	current_user.setScore(addScore(current_user, score));
	    	
	    	//UPDATE USERS SCORE
	    	try {
				UserService.updateScore(current_user);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
	    	
	    	//UPDATE USERS ANSWERED QUESTIONS
	    	try {
				UserService.updateAnswersQuestions(questionsAnswered);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
    		
    	} else {
    		result.put("valid", false);
    	}
    	
    	Status status = null;
    	try{
    		status = ok(result);
    	} catch (Exception e) {
    		status = unauthorized();
    	}
    	
    	
    	return status;
    }
    
    public static <E> Collection<E> makeCollection(Iterable<E> iter) {
        Collection<E> list = new ArrayList<E>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }
    
    public static String addScore(User user, int score) {
    	return (Integer.parseInt(user.getScore()) + score) + "";
    }
   
    
}
