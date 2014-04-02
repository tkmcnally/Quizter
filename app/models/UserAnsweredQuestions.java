package models;

import com.mongodb.BasicDBList;
import org.jongo.marshall.jackson.oid.Id;

public class UserAnsweredQuestions {

    @Id
    private String _id;

    private BasicDBList questionsAnswered;

    public String get_id() {
        return _id.replace("\"", "");
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public BasicDBList getQuestionsAnswered() {
        return questionsAnswered;
    }

    public void setQuestionsAnswered(BasicDBList questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
    }

}
