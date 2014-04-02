package models;

import com.mongodb.BasicDBList;

/**
 * Data Model representing a Quizter QuizterUser.
 * @author Thomas McNally
 *
 */
public class NewQuizterUser extends QuizterUser {
	
	
	private BasicDBList questionsAnswered;
	
	public NewQuizterUser() {
		this.questionsAnswered = new BasicDBList();
	}

	public BasicDBList getQuestionsAnswered() {
		return questionsAnswered;
	}

	public void setQuestionsAnswered(BasicDBList questionsAnswered) {
		this.questionsAnswered = questionsAnswered;
	}
	
	public NewQuizterUser mapUser(QuizterUser user) {
		this.setName(user.getName());
		this.set_id(user.get_id());
		return this;
	}
}
