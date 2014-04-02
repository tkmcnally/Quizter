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
import play.mvc.Results;
import services.UserService;
import utils.Util;

import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Thomas on 4/2/2014.
 */
public class ProfileController extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result getProfile() {

        Results.Status status = null;

        //JSON object from request.
        JsonNode requestJson = request().body().asJson();


        String access_token = requestJson.findPath("ACCESS_TOKEN").textValue();
        String screen_density = requestJson.findPath("screen_density").textValue();

        if (access_token == null || screen_density == null) {
            return badRequest("Bad JSON: Invalid parameters.");
        }

        //Public facebook client accessor
        FacebookClient facebookClient = new DefaultFacebookClient(access_token);


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

        QuizterUser tempUser = new QuizterUser();
        tempUser.mapFacebookUser(facebookUser);
        QuizterUser user = authentication(tempUser);

        if (user != null) {
            //Return JSON.
            ObjectNode result = Json.newObject();
            result.put("id", user.get_id());
            result.put("name", user.getName());
            result.put("questions", user.getQuestions().toString());
            result.put("date_created", user.getDateCreated());
            result.put("photo_url", facebookUser.getData().getUrl());
            result.put("score", user.getScore());

            boolean hasSetupProfile = Util.hasSetupProfile(user);
            result.put("setup_profile", hasSetupProfile);

            System.out.println("still working");
            status = ok(result);
        } else {
            status = unauthorized();
        }
        return status;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateQuestions() {

        String questionsString = null;

        //JSON object from request.
        JsonNode requestJson = request().body().asJson();

        //Check for un/pw
        String access_token = requestJson.findPath("ACCESS_TOKEN").textValue();
        String screen_density = requestJson.findPath("screen_density").textValue();
        JsonNode userQuestions = requestJson.findPath("updated_questions");


        if (access_token == null || screen_density == null || userQuestions == null || userQuestions.size() == 0) {
            return badRequest("Bad JSON: Invalid parameters.");
        }

        //Public facebook client accessor
        FacebookClient facebookClient = new DefaultFacebookClient(access_token);

        FacebookUser facebookUser = null;
        try {
            facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
        } catch (FacebookOAuthException e) {
            return unauthorized("OAuth Exception: Invalid authentication token.");
        }
        QuizterUser updatedUser = new QuizterUser();
        updatedUser.mapFacebookUser(facebookUser);

        String resultString = userQuestions.toString();
        resultString = resultString.replace(":\"[", ": [");
        resultString = resultString.replace("]\"", "]");
        resultString = resultString.replace("\\\"", "\"");
        StringBuilder rS = new StringBuilder(resultString);
        if (rS.charAt(0) != '[') {
            rS.delete(0, 1);
        }

        try {
            questionsString = rS.toString();
            Object o = com.mongodb.util.JSON.parse(questionsString);
            BasicDBList obj = (BasicDBList) o;

            updatedUser.setQuestions(Util.hashQuestions(obj));
        } catch (Exception e) {
            e.printStackTrace();
            Status status = null;
            return (status = ok(e.toString() + " " + e.getMessage() + " " + e.getStackTrace().toString() + " " + "\n" + questionsString));
        }
        try {
            UserService.updateQuestions(updatedUser);

        } catch (Exception e) {


            PrintStream s = null;
            e.printStackTrace();
            //questions.add(s);
        }

        Status status = null;


        facebookUser.combine(facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class,
                Parameter.with("width", Util.getPictureSize(screen_density)), Parameter.with("redirect", false),
                Parameter.with("height", Util.getPictureSize(screen_density)), Parameter.with("type", "normal")));

        QuizterUser tempUser = new QuizterUser();
        tempUser.mapFacebookUser(facebookUser);
        QuizterUser user = ProfileController.authentication(tempUser);


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

    @BodyParser.Of(BodyParser.Json.class)
    public static Result loadQuestions() {

        //JSON object from request.
        JsonNode requestJson = request().body().asJson();

        //Check for un/pw
        String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
        String userQuestions_index = requestJson.findPath("current_end_index").textValue();

        if (ACCESS_TOKEN == null || userQuestions_index == null) {
            return badRequest("Bad JSON: Invalid parameters.");
        }

        //Public facebook client accessor
        FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);

        FacebookUser facebookUser = null;
        try {
            facebookUser = facebookClient.fetchObject("me", FacebookUser.class);
        } catch (FacebookOAuthException e) {
            return unauthorized("OAuth Exception: Invalid authentication token.");
        }

        QuizterUser updatedUser = new QuizterUser();
        updatedUser.mapFacebookUser(facebookUser);

        int index = Integer.parseInt(userQuestions_index);
        Status status = null;

        try {
            Iterable<Question> questions = UserService.loadQuestionsFromIndex(index);
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



    public static QuizterUser authentication(final QuizterUser newUser) {
        QuizterUser user = null;
        try {
            user = UserService.userAlreadyExists(newUser);
            if (user == null) {
                user = UserService.registerUser(newUser);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return user;
    }



}
