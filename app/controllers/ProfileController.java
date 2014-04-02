package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import models.FacebookUser;
import models.Question;
import models.QuizterUser;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import services.DatabaseService;
import utils.Util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Thomas on 4/2/2014.
 */
public class ProfileController extends Controller {


    /**
     * Retrieving information used to load the profile page.
     *
     * @return JSON with relevant profile information.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result getProfile() {

        //Returned HTTP status code.
        Status status = null;

        //Parse JSON into JsonNode from incoming HTTP request.
        JsonNode requestJson = request().body().asJson();

        //Retrieve expected attributes from JSON.
        String access_token = requestJson.findPath("ACCESS_TOKEN").textValue();
        String screen_density = requestJson.findPath("screen_density").textValue();
        if (access_token == null || screen_density == null) {
            return badRequest("Bad JSON: Invalid parameters.");
        }

        //Create Facebook API accessor with user token.
        FacebookClient facebookClient = new DefaultFacebookClient(access_token);

        //Retrieve QuizterUser's facebook profile information.
        FacebookUser facebookUser = null;
        try {
            //Create mock FacebookUser object with info from Facebook.
            facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
            facebookUser.combine(facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class,
                    Parameter.with("width", Util.getPictureSize(screen_density)), Parameter.with("redirect", false),
                    Parameter.with("height", Util.getPictureSize(screen_density)), Parameter.with("type", "normal")));
        } catch (FacebookOAuthException e) {
            return unauthorized("OAuth Exception: Invalid authentication token.");
        }

        //Create QuziterUser Models.
        QuizterUser tempUser = new QuizterUser();
        tempUser.mapFacebookUser(facebookUser);
        QuizterUser user = null;
        try {
            user = authentication(tempUser);
        } catch (UnknownHostException e) {
            return internalServerError("Database Connection Error: Could not connect to database.");
        }

        if (user != null) {
            //Return JSON.
            ObjectNode result = Json.newObject();
            result.put("id", user.get_id());
            result.put("name", user.getName());
            result.put("questions", user.getQuestions().toString());
            result.put("date_created", user.getDateCreated());
            result.put("photo_url", facebookUser.getData().getUrl());
            result.put("score", user.getScore());

            // Determine if User has setup their 5 questions.
            boolean hasSetupProfile = Util.hasSetupProfile(user);
            result.put("setup_profile", hasSetupProfile);

            status = ok(result);
        } else {
            status = unauthorized();
        }
        return status;
    }

    /**
     * Update a users profile.
     *
     * @return the newly updated user's information as a confirmation.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateProfile() {

        //Returned HTTP status code.
        Status status = null;

        //Parse JSON into JsonNode from incoming HTTP request.
        JsonNode requestJson = request().body().asJson();


        //Retrieve expected attributes from JSON.
        String access_token = requestJson.findPath("ACCESS_TOKEN").textValue();
        String screen_density = requestJson.findPath("screen_density").textValue();
        JsonNode userQuestions = Json.parse(requestJson.findPath("updated_questions").asText());
        if (access_token == null || screen_density == null || userQuestions == null || userQuestions.size() == 0) {

            return badRequest("Bad JSON: Invalid parameters.");
        }

        //Create Facebook API accessor with user token.
        FacebookClient facebookClient = new DefaultFacebookClient(access_token);

        //Retrieve QuizterUser's facebook profile information.
        FacebookUser facebookUser = null;
        try {
            facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
        } catch (FacebookOAuthException e) {
            return unauthorized("OAuth Exception: Invalid authentication token.");
        }

        //Create QuziterUser model.
        QuizterUser updatedUser = new QuizterUser();
        updatedUser.mapFacebookUser(facebookUser);


        // Filter stale escape characters from JSON.
        String resultString = userQuestions.toString();
        resultString = resultString.replace(":\"[", ": [");
        resultString = resultString.replace("]\"", "]");
        resultString = resultString.replace("\\\"", "\"");
        StringBuilder rS = new StringBuilder(resultString);
        if (rS.charAt(0) != '[') {
            rS.delete(0, 1);
        }

        // Construct new question array for JSON.
        String questionsString = null;
        try {
            questionsString = rS.toString();
            Object o = com.mongodb.util.JSON.parse(questionsString);
            BasicDBList obj = (BasicDBList) o;

            updatedUser.setQuestions(Util.hashQuestions(obj));

            DatabaseService.updateQuestions(updatedUser);
        } catch (Exception e) {
            return internalServerError("Database Connection Error: Could not connect to database.");
        }


        //Combine FacebookUser model with another query.
        facebookUser.combine(facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class,
                Parameter.with("width", Util.getPictureSize(screen_density)), Parameter.with("redirect", false),
                Parameter.with("height", Util.getPictureSize(screen_density)), Parameter.with("type", "normal")));

        //Create QuziterUser models.
        QuizterUser tempUser = new QuizterUser();
        tempUser.mapFacebookUser(facebookUser);


        QuizterUser user = null;
        try {
            user = ProfileController.authentication(tempUser);
        } catch (UnknownHostException e) {
            return internalServerError("Database Connection Error: Could not connect to database.");
        }


        // Construct JSON for return
        if (user != null) {
            //Return JSON.
            ObjectNode result = Json.newObject();
            result.put("id", user.get_id());
            result.put("name", user.getName());
            result.put("questions", updatedUser.getQuestions().toString());
            result.put("date_created", user.getDateCreated());
            result.put("photo_url", facebookUser.getData().getUrl());
            result.put("score", user.getScore());

            boolean hasSetupProfile = Util.hasSetupProfile(user);
            result.put("setup_profile", hasSetupProfile);

            status = ok(result);
        } else {
            status = unauthorized();
        }
        return status;
    }


    /**
     * Grabs a list of questions from a specified index in the database
     *
     * @return list of default questions retrieved from database
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result loadQuestions() {

        //Returned HTTP status code.
        Status status = null;

        //Parse JSON into JsonNode from incoming HTTP request.
        JsonNode requestJson = request().body().asJson();

        //Retrieve expected attributes from JSON.
        String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
        String userQuestions_index = requestJson.findPath("current_end_index").textValue();

        if (ACCESS_TOKEN == null || userQuestions_index == null) {
            return badRequest("Bad JSON: Invalid parameters.");
        }

        //Create Facebook API accessor with user token.
        FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);

        //Retrieve QuizterUser's facebook profile information.
        FacebookUser facebookUser = null;
        try {
            facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
        } catch (FacebookOAuthException e) {
            return unauthorized("OAuth Exception: Invalid authentication token.");
        }

        //Create QuziterUser Models.
        QuizterUser updatedUser = new QuizterUser();
        updatedUser.mapFacebookUser(facebookUser);

        //Parse integer from JSON attribute.
        int index = 0;
        try {
            index = Integer.parseInt(userQuestions_index);
        } catch (NumberFormatException e) {
            return badRequest("Bad JSON: Invalid parameters.");
        }

        //Construct questions JSON array from database returned iterable.
        try {
            Iterable<Question> questions = DatabaseService.loadQuestionsFromIndex(index);
            ArrayList<String> questionList = new ArrayList<String>();
            Iterator<Question> iter = questions.iterator();
            while (iter.hasNext()) {
                Question next = iter.next();
                questionList.add(next.getQuestion());
            }
            BasicDBList newList = new BasicDBList();
            for (String q : questionList) {
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


    /**
     * @param newUser User to be registered or retrieved from database
     * @return QuizterUser model populated with database attributes.
     */
    public static QuizterUser authentication(final QuizterUser newUser) throws UnknownHostException {
        QuizterUser user = null;
        user = DatabaseService.userAlreadyExists(newUser);
        if (user == null) {
            user = DatabaseService.registerUser(newUser);
        }
        return user;
    }


}
