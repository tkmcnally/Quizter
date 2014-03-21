package services;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import models.FacebookUser;
import models.NewUser;
import models.Question;
import models.Score;
import models.User;
import models.UserAnsweredQuestions;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import utils.Constants;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.restfb.json.JsonObject;

import static org.jongo.Oid.withOid;


/**
 * UserService --- class to handle all database operations.
 * @author Thomas McNally
 */
public class UserService {

	
	/**
	 * Returns a Collection connection to a Database.
	 * @param dbName - Name of Database to connect.
	 * @param collectionName - Name of Collection to connect.
	 * @return mongoCollection - A MongoCollection established using Jongo.
	 * @throws UnknownHostException - If connection cannot be established to database.
	 */
	public static MongoCollection getConnection(String dbName, String collectionName) throws UnknownHostException {
		MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://tkmcnally:t5725555@paulo.mongohq.com:10051/" + Constants.DB_NAME));
		DB db = mongoClient.getDB(dbName);
		Jongo jongo = new Jongo(db);
		MongoCollection mongoCollection = jongo.getCollection(collectionName);
		
		return mongoCollection;
	}
	
	/**
	 * Validates if the user exists in the Database already.
	 * @param user - User object to validate .
	 * @return reqUser - User object retrieved from Database matching param user.
	 * @throws UnknownHostException - If connection cannot be established to database.
	 */
	public static User userAlreadyExists(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		User reqUser = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(User.class);
		
		return reqUser;
	}
	
	/**
	 * Registers the user into the Database.
	 * @param user - User object to register.
	 * @return reqUser - User object retrieved from Database matching param user
	 * after insert operation.
	 * @throws UnknownHostException - If connection cannot be established to database.
	 */
	public static User registerUser(User user) throws UnknownHostException {
		NewUser newUser = (NewUser) user;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		newUser.setDateCreated(dateFormat.format(cal.getTime()));
		newUser.setScore("0");
		
		BasicDBList newList = new BasicDBList();
		for(int i = 0; i < 5; i++) {
			BasicDBObject obj = new BasicDBObject(2);
			obj.put("question", "Select a question!");
			obj.put("answer", "");
			newList.add(obj);
		}

		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.insert(newUser);
		
		return newUser;
	}
	
	/**
	 * Update the registered questions of a User
	 * @param user - User to update
	 * @return reqUser - User object retrieved from Database matching param user
	 * after update operation.
	 * @throws UnknownHostException - If connection cannot be established to database.
	 */
	public static User updateQuestions(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.update("{_id: '" + user.get_id() + "'}").with("{$set: {questions: " + user.getQuestions() + "}}");
		
		User reqUser = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(User.class);
		return reqUser;
		
	}
	
	/**
	 * Retrieve a list of 10 questions.
	 * @param index - End index location of the questions to load.
	 * @return questionList - A list of Question objects retrieved from the database.
	 * @throws UnknownHostException - If connection cannot be established to database.
	 */
	public static Iterable<Question> loadQuestionsFromIndex(int index) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "questions");
		
		Iterable<Question> questionList = mongoCollection.find("{_id: {$gt: " + (index - 10) + ", $lte: " + index + "}}").as(Question.class);
		return questionList;
	}

	public static void updateScore(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.update("{_id: '" + user.get_id() + "'}").with("{$set: {score: " + user.getScore() + "}}");
	}
	
	/**
	 * Retrieve a list of players for User's friends.
	 * @param players - List of player's IDs taken from User's friends list.
	 * @return player_list - A list of Users queried from the DB with ID's matching those in players.
	 * @throws UnknownHostException - If connection cannot be established to database.
	 */
	public static User getPlayerForFriend(List<JsonObject> players, int index) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		int current_player = 0;
		User user = null;
		User tempUser = null;
		
		List<String> user_ids = new ArrayList<String>();
		for(JsonObject obj: players) {
			user_ids.add(obj.getString("id"));
		}

		Iterable users = mongoCollection.find("{_id: {$in:#}}", user_ids).as(User.class);
		Iterator iter = users.iterator();
		while(iter.hasNext()) {
			tempUser = (User) iter.next();
			if(current_player == index) {
				user = tempUser;
				break;
			} else if(current_player < index) {
				current_player++;
			} else {
				break;
			}
		}
		
		return user;
	}
	
	public static Iterable<User> getFriendsOfPlayer(List<JsonObject> players) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		int current_player = 0;
		User user = null;
		User tempUser = null;
		
		List<String> user_ids = new ArrayList<String>();
		for(JsonObject obj: players) {
			user_ids.add(obj.getString("id"));
		}

		Iterable<User> users = mongoCollection.find("{_id: {$in:#}}", user_ids).as(User.class);
		Iterator<User> iter = users.iterator();
		
		return users;
	}
	
	
	public static UserAnsweredQuestions getQuestionsAnswered(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		
		UserAnsweredQuestions userAnsweredQuestions = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(UserAnsweredQuestions.class);
		return userAnsweredQuestions;
		
	}
	
public static void updateAnswersQuestions(UserAnsweredQuestions userAnsweredQuestion) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.update("{_id: '" + userAnsweredQuestion.get_id() + "'}").with("{$set: {questionsAnswered: " + userAnsweredQuestion.getQuestionsAnswered() + "}}");
	}
}
