package models;

/**
 * Data Model representing the Score of a Quizter User.
 * @author Thomas McNally
 *
 */
public class Score {

	private String _id;

	private String score;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
	
	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}
}
