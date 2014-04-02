package models;

import com.mongodb.BasicDBList;
import org.jongo.marshall.jackson.oid.Id;

/**
 * Data Model representing a Quizter QuizterUser.
 *
 * @author Thomas McNally
 */
public class QuizterUser {

    @Id
    private String _id;

    private String email;

    private String name;

    private String dateCreated;

    private BasicDBList questions;

    private String picture_url;

    private String score;

    public void mapFacebookUser(FacebookUser facebookUser) {
        this._id = facebookUser.getId();
        this.name = facebookUser.getName();
        if (facebookUser.getData() != null) {
            this.picture_url = facebookUser.getData().getUrl();
        }
    }

    public String get_id() {
        return _id.replace("\"", "");
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public BasicDBList getQuestions() {
        return questions;
    }

    public void setQuestions(BasicDBList questions) {
        this.questions = questions;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }


}
