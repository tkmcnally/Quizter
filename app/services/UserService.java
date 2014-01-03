package services;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import models.FacebookUser;
import models.User;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import utils.Constants;

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
		
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.insert(newUser);
		
		return newUser;
	}
	
	public static User updateQuestions(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.update(user.get_id()).with(user.getQuestions());
		
		return user;
		
	}
}
