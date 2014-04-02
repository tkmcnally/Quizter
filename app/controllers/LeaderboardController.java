package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import models.FacebookUser;
import models.QuizterUser;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.ScoreComparator;
import services.UserService;
import utils.Util;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Thomas on 4/2/2014.
 */
public class LeaderboardController extends Controller {

    /**
     * Returns JSON with list of users in descending order with respect to User's score.
     * @return a leaderboard for the user
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result loadLeaderboard() {

        //Returned HTTP status code.
        Status status = null;

        //Parse JSON into JsonNode from incoming HTTP request.
        JsonNode requestJson = request().body().asJson();

        //Retrieve expected attributes from JSON.
        String ACCESS_TOKEN = requestJson.findPath("ACCESS_TOKEN").textValue();
        if (ACCESS_TOKEN == null || ACCESS_TOKEN.isEmpty()) {
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

        //Combine FacebookUser model with another query.
        facebookUser.combine(facebookClient.fetchObject(facebookUser.getId() + "/picture", FacebookUser.class, Parameter.with("redirect", false), Parameter.with("type", "normal")));

        //Create Quziter UserModels.
        QuizterUser quizter_user = null;
        QuizterUser temp_fb_user = new QuizterUser();
        temp_fb_user.mapFacebookUser(facebookUser);

        //Retrieve user from database, and set photo url from previously created FacebookUser model.
        try {
            quizter_user = UserService.userAlreadyExists(temp_fb_user);
            quizter_user.setPicture_url(temp_fb_user.getPicture_url());
        } catch (UnknownHostException e2) {
            return internalServerError("Database Connection Error: Could not connect to database.");
        }

        //Retrieve facebook friends for User.
        com.restfb.Connection<JsonObject> myFriends = facebookClient.fetchConnection("me/friends", JsonObject.class, Parameter.with("fields", "picture,name"));

        //Create JSON container.
        ObjectNode child = Json.newObject();

        //Create array for JSON leaderboard attribute.
        ArrayNode apps = child.putArray("leaderboard");

        //Traverse facebook friends list and create temporary place holder list.
        HashMap<String, HashMap<String, String>> friends_as_users = new HashMap<String, HashMap<String, String>>();
        for (JsonObject u : myFriends.getData()) {
            HashMap<String, String> map1 = new HashMap<String, String>();
            map1.put("photo", u.getJsonObject("picture").getJsonObject("data").getString("url"));
            map1.put("name", u.getString("name"));

            friends_as_users.put(u.getString("id"), map1);
        }

        //Retrieve list of Facebook Users that have a Quizter account.
        Iterable<QuizterUser> db_users = null;
        try {
            db_users = UserService.getFriendsOfPlayer(myFriends.getData());
        } catch (UnknownHostException e1) {
            return internalServerError("Database Connection Error: Could not connect to database.");
        }


        //Construct returned JSON.
        if (db_users != null) {

            //Convert iterable to list for easy sorting.
            List<QuizterUser> users = (List<QuizterUser>) Util.makeCollection(db_users);

            //Add current user to the list of users with Quizter accounts.
            users.add(quizter_user);

            //Sort list of users by Score.
            Collections.sort(users, new ScoreComparator());

            //Add each user from the sorted list to the JSON array, with relevant information.
            for (int i = 0; i < users.size(); i++) {

                QuizterUser temp_user = users.get(i);

                ObjectNode uNode = Json.newObject();
                uNode.put("id", temp_user.get_id());
                uNode.put("rank", i + 1);

                if (friends_as_users.get(temp_user.get_id()) == null) {
                    uNode.put("name", temp_user.getName());
                    uNode.put("photo_url", temp_user.getPicture_url());
                } else {
                    uNode.put("name", friends_as_users.get(temp_user.get_id()).get("name"));
                    uNode.put("photo_url", friends_as_users.get(temp_user.get_id()).get("photo"));
                }
                uNode.put("score", temp_user.getScore());
                apps.add(uNode);
            }
        }

        try {
            status = ok(child);
        } catch (Exception e) {
            status = internalServerError("Unexpected Error.");
        }
        return status;
    }


}
