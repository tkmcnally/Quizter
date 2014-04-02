package services;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import models.*;
import models.QuizterUser;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import utils.Constants;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.restfb.json.JsonObject;


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
     * @param user - QuizterUser object to validate .
     * @return reqUser - QuizterUser object retrieved from Database matching param user.
     * @throws UnknownHostException - If connection cannot be established to database.
     */
    public static QuizterUser userAlreadyExists(QuizterUser user) throws UnknownHostException {
        MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
        QuizterUser reqUser = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(QuizterUser.class);

        return reqUser;
    }

    /**
     * Registers the user into the Database.
     * @param user - QuizterUser object to register.
     * @return reqUser - QuizterUser object retrieved from Database matching param user
     * after insert operation.
     * @throws UnknownHostException - If connection cannot be established to database.
     */
    public static QuizterUser registerUser(QuizterUser user) throws UnknownHostException {
        QuizterUser newUser = user;

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

        user.setQuestions(newList);

        MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
        mongoCollection.insert(newUser);

        mongoCollection.update("{_id: '" + user.get_id() + "'}").with("{$set: {questionsAnswered: [] }}");

        return newUser;
    }

    /**
     * Update the registered questions of a QuizterUser
     * @param user - QuizterUser to update
     * @return reqUser - QuizterUser object retrieved from Database matching param user
     * after update operation.
     * @throws UnknownHostException - If connection cannot be established to database.
     */
    public static QuizterUser updateQuestions(QuizterUser user) throws UnknownHostException {
        MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
        mongoCollection.update("{_id: '" + user.get_id() + "'}").with("{$set: {questions: " + user.getQuestions() + "}}");

        QuizterUser reqUser = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(QuizterUser.class);
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

    public static void updateScore(QuizterUser user) throws UnknownHostException {
        MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
        mongoCollection.update("{_id: '" + user.get_id() + "'}").with("{$set: {score: " + user.getScore() + "}}");
    }

    /**
     * Retrieve a list of players for QuizterUser's friends.
     * @param players - List of player's IDs taken from QuizterUser's friends list.
     * @return player_list - A list of Users queried from the DB with ID's matching those in players.
     * @throws UnknownHostException - If connection cannot be established to database.
     */
    public static QuizterUser getPlayerForFriend(List<JsonObject> players, int index) throws UnknownHostException {
        MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
        int current_player = 0;
        QuizterUser user = null;
        QuizterUser tempUser = null;

        List<String> user_ids = new ArrayList<String>();
        for(JsonObject obj: players) {
            user_ids.add(obj.getString("id"));
        }

        Iterable users = mongoCollection.find("{_id: {$in:#}}", user_ids).as(QuizterUser.class);
        Iterator iter = users.iterator();
        while(iter.hasNext()) {
            tempUser = (QuizterUser) iter.next();
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

    public static Iterable<QuizterUser> getFriendsOfPlayer(List<JsonObject> players) throws UnknownHostException {
        MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
        int current_player = 0;
        QuizterUser user = null;
        QuizterUser tempUser = null;

        List<String> user_ids = new ArrayList<String>();
        for(JsonObject obj: players) {
            user_ids.add(obj.getString("id"));
        }

        Iterable<QuizterUser> users = mongoCollection.find("{_id: {$in:#}}", user_ids).as(QuizterUser.class);
        Iterator<QuizterUser> iter = users.iterator();

        return users;
    }


    public static UserAnsweredQuestions getQuestionsAnswered(QuizterUser user) throws UnknownHostException {
        MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");

        UserAnsweredQuestions userAnsweredQuestions = mongoCollection.findOne("{_id: '" + user.get_id() +"'}").as(UserAnsweredQuestions.class);
        return userAnsweredQuestions;

    }

    public static void updateAnswersQuestions(UserAnsweredQuestions userAnsweredQuestion) throws UnknownHostException {
        MongoCollection mongoCollection = getConnection(Constants.DB_NAME, "users");
        mongoCollection.update("{_id: '" + userAnsweredQuestion.get_id() + "'}").with("{$set: {questionsAnswered: " + userAnsweredQuestion.getQuestionsAnswered() + "}}");
    }
}
