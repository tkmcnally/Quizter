package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import models.FacebookUser;
import models.QuizterUser;
import models.UserAnsweredQuestions;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.UserService;
import utils.Util;
import utils.WordMatcher;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Thomas on 4/2/2014.
 */
public class QuizController extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    public static Result submitQuiz() {


        String questionKey = null;
        String questionsString = null;

        //JSON object from request.
        JsonNode requestJson = request().body().asJson();

        String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
        String PLAYER_ID = requestJson.findPath("player_id").textValue();
        String screen_density = requestJson.findPath("screen_density").textValue();
        JsonNode userQuestions = requestJson.findPath("question_answer");

        if (ACCESS_TOKEN == null || PLAYER_ID == null || screen_density == null || userQuestions == null || userQuestions.size() == 0) {
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
        QuizterUser current_user = new QuizterUser();
        current_user.mapFacebookUser(facebookUser);

        FacebookUser friend = facebookClient.fetchObject(PLAYER_ID + "/picture", FacebookUser.class,
                Parameter.with("width", Util.getPictureSize(screen_density)), Parameter.with("redirect", false),
                Parameter.with("height", Util.getPictureSize(screen_density)), Parameter.with("type", "normal"));


        List<String> questions = new ArrayList<String>();
        questions.add(userQuestions.elements() + "");

        String resultString = userQuestions.toString();
        resultString = resultString.replace(":\"[", ": [");
        resultString = resultString.replace("]\"", "]");
        resultString = resultString.replace("\\\"", "\"");
        StringBuilder rS = new StringBuilder(resultString);
        //rS.delete(0, 1);

        questionsString = rS.toString();
        Object o = com.mongodb.util.JSON.parse(questionsString);
        BasicDBList dbObj = (BasicDBList) o;


        QuizterUser updatedUser = new QuizterUser();
        updatedUser.setQuestions(dbObj);


        //Create temporary user with ID from JSON
        QuizterUser tempUser = new QuizterUser();
        tempUser.set_id(PLAYER_ID);

        QuizterUser quiz_player = null;
        try {
            quiz_player = UserService.userAlreadyExists(tempUser);
            current_user = UserService.userAlreadyExists(current_user);
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        List<String> old_questions = new ArrayList<String>();
        List<String> old_answers = new ArrayList<String>();
        for (Object obj : updatedUser.getQuestions()) {
            LinkedHashMap lhm = (LinkedHashMap) obj;
            old_questions.add((String) lhm.get("question"));
            old_answers.add((String) lhm.get("answer"));
        }

        List<String> new_questions = new ArrayList<String>();
        List<String> new_answers = new ArrayList<String>();
        for (Object obj : quiz_player.getQuestions()) {
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
        if (new_questions.containsAll(old_questions)) {
            result.put("valid", true);

            BasicDBList question_overview = new BasicDBList();
            int score = 0;
            for (int i = 0; i < old_answers.size(); i++) {
                BasicDBObject obj = new BasicDBObject(3);
                obj.put("question", old_questions.get(i));
                obj.put("given_answer", old_answers.get(i));
                if (!WordMatcher.doesMatch(old_answers.get(i), new_answers.get(i))) {
                    obj.put("correct_answer", "false");
                } else {
                    String hash = (new_answers.get(i) + new_questions.get(i)).hashCode() + "";
                    System.out.println(questionsAnswered.getQuestionsAnswered());
                    if (questionsAnswered == null || questionsAnswered.getQuestionsAnswered() == null || questionsAnswered.getQuestionsAnswered().isEmpty()) {
                        obj.put("already_answered", "false");
                        questionsAnswered.setQuestionsAnswered(new BasicDBList());
                        questionsAnswered.getQuestionsAnswered().add(hash);
                        score++;
                    } else if (!questionsAnswered.getQuestionsAnswered().contains(hash)) {
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

        Results.Status status = null;
        try {
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

        String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
        String PLAYER_ID = requestJson.findPath("player_id").textValue();

        if (ACCESS_TOKEN == null || PLAYER_ID == null) {
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

        //Create temporary user with ID from JSON
        QuizterUser tempUser = new QuizterUser();
        tempUser.set_id(PLAYER_ID);

        //Retrieve QuizterUser from database
        QuizterUser PLAYER = null;
        try {
            PLAYER = UserService.userAlreadyExists(tempUser);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        //Construct question list
        BasicDBList questionDBList = PLAYER.getQuestions();
        BasicDBList filtered_questions = new BasicDBList();
        for (Object obj : questionDBList) {
            LinkedHashMap qa = (LinkedHashMap) obj;
            qa.remove("answer");
            filtered_questions.add(qa);
        }

        //Construct JSON to return
        ObjectNode result = Json.newObject();
        result.put("questions", filtered_questions.toString());
        result.put("player_id", PLAYER_ID);


        Status status = null;
        try {
            status = ok(result);
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
        if (ACCESS_TOKEN == null || PLAYER_INDEX == null) {
            return badRequest("Bad JSON: Invalid parameters.");
        }


        int int_player_index = 0;
        try {
            int_player_index = Integer.parseInt(PLAYER_INDEX);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //Public facebook client accessor
        FacebookClient facebookClient = new DefaultFacebookClient(ACCESS_TOKEN);
        com.restfb.Connection<JsonObject> myFriends = null;
        try {
            myFriends = facebookClient.fetchConnection("me/friends", JsonObject.class, Parameter.with("fields", "picture.height(400),name"));
        } catch (FacebookOAuthException e) {
            return unauthorized("OAuth Exception: Invalid authentication token.");
        }

        List<JsonObject> friends_list = myFriends.getData();

        //Query DB for friends with ID
        QuizterUser user_quizter_friend = null;
        try {
            user_quizter_friend = UserService.getPlayerForFriend(friends_list, int_player_index);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        ObjectNode result = Json.newObject();
        if (user_quizter_friend != null) {
            FacebookUser user_with_picture = facebookClient.fetchObject(user_quizter_friend.get_id() + "/picture", FacebookUser.class,
                    Parameter.with("width", 400), Parameter.with("redirect", false),
                    Parameter.with("height", 400), Parameter.with("type", "normal"));

            result.put("id", user_quizter_friend.get_id());
            result.put("name", user_quizter_friend.getName());
            result.put("photo_url", user_with_picture.getData().getUrl());
            result.put("available_players", "true");

        } else {
            result.put("available_players", "false");
        }


        Status status = null;
        try {
            status = ok(result);
        } catch (Exception e) {
            status = unauthorized();
        }

        return status;

    }


    public static String addScore(QuizterUser user, int score) {
        return (Integer.parseInt(user.getScore()) + score) + "";
    }

}
