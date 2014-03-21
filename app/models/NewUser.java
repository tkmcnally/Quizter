package models;

import java.util.ArrayList;

import org.jongo.marshall.jackson.oid.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
/**
 * Data Model representing a Quizter User.
 * @author Thomas McNally
 *
 */
public class NewUser extends User {
	
	
	private BasicDBList questionsAnswered;
	
	public NewUser() {
		this.questionsAnswered = new BasicDBList();
	}

	public BasicDBList getQuestionsAnswered() {
		return questionsAnswered;
	}

	public void setQuestionsAnswered(BasicDBList questionsAnswered) {
		this.questionsAnswered = questionsAnswered;
	}
	
	public NewUser mapUser(User user) {
		this.setName(user.getName());
		this.set_id(user.get_id());
		return this;
	}
}
