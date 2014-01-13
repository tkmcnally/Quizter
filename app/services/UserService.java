package services;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import models.FacebookUser;
import models.Question;
import models.Score;
import models.User;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import utils.Constants;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class UserService {

	public static MongoCollection getConnection(String dbName, String collectionName) throws UnknownHostException {
		MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://tkmcnally:t5725555@paulo.mongohq.com:10051/" + Constants.DB_NAME));
		DB db = mongoClient.getDB(dbName);
		Jongo jongo = new Jongo(db);
		MongoCollection mongoCollection = jongo.getCollection(collectionName);
		
		return mongoCollection;
	}
	
	public static User userAlreadyExists(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		User reqUser = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(User.class);
		
		return reqUser;
	}
	
	public static User registerUser(User user) throws UnknownHostException {
		User newUser = user;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		newUser.setDateCreated(dateFormat.format(cal.getTime()));
		
		BasicDBList newList = new BasicDBList();
		for(int i = 0; i < 5; i++) {
			BasicDBObject obj = new BasicDBObject(2);
			obj.put("question", "Select a question!");
			obj.put("answer", "");
			newList.add(obj);
		}
		
		user.setQuestions(newList);
		
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.insert(newUser);
		
		
		Score score = new Score();
		score.set_id(user.get_id());
		score.setScore(10 + "");
		
		mongoCollection = getConnection(Constants.DB_NAME, "scores");
		mongoCollection.insert(score);
		
		return newUser;
	}
	
	public static User updateQuestions(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.update("{_id: '" + user.get_id() + "'}").with("{$set: {questions: " + user.getQuestions() + "}}");
		
		
		User reqUser = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(User.class);
		return reqUser;
		
	}
	
	public static Iterable<Question> loadQuestionsFromIndex(int index) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "questions");
		
		Iterable<Question> questionList = mongoCollection.find("{_id: {$gt: " + (index - 10) + ", $lte: " + index + "}}").as(Question.class);
		return questionList;
	}
	
	public static void updateScore(Score score) throws UnknownHostException {
		
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "scores");
		mongoCollection.save(score);
		
	}
	
	public static Score retrieveScore(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "scores");
		
		Score score = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(Score.class);
		return score;
	}
}
