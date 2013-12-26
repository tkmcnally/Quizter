package services;

import java.net.UnknownHostException;

import models.User;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import utils.Constants;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class UserService {

	public static MongoCollection getConnection(String dbName, String collectionName) throws UnknownHostException {
		MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://tkmcnally:t5725555@paulo.mongohq.com:10055/app20608490"));
		DB db = mongoClient.getDB(dbName);
		Jongo jongo = new Jongo(db);
		MongoCollection mongoCollection = jongo.getCollection(collectionName);
		
		return mongoCollection;
	}
	
	public static boolean userAlreadyExists(String email) throws UnknownHostException {
		boolean userExists = false;
		
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		User reqUser = mongoCollection.findOne("{email: '" + email + "'}").as(User.class);
		if(reqUser != null) {
			userExists = true;
		}
		
		return userExists;
	}
	
	public static void registerUser(User user) throws UnknownHostException {
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		mongoCollection.insert(user);
	}
	
	public static boolean authenticateUser(User user) throws UnknownHostException {
		boolean authenticated = false;
		
		MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
		User reqUser = mongoCollection.findOne("{email: '" + user.getEmail() + "', password: '" + user.getPassword() + "'}").as(User.class);
		if(reqUser != null) {
			authenticated = true;
		}
		
		return authenticated;
	}
	
}
