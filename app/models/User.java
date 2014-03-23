package models;

import java.util.ArrayList;

import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
/**
 * Data Model representing a Quizter User.
 * @author Thomas McNally
 *
 */
public class User {
	
		@Id
		private String _id;

		private String email;
		
		private String name;
		
		private String dateCreated;
		
		private BasicDBList questions;
		
		private String picture_url;
		
		private String score;

		public void mapFacebookUser(FacebookUser facebookUser) {
			this._id = facebookUser.id;
			this.name = facebookUser.name;
			if(facebookUser.getData() != null) {
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
		
		public String getName() {
			return name;
		}
		
		public void setEmail(String email) {
			this.email = email;
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
		
		public 	BasicDBList getQuestions() {
			return questions;
		}

		public void setQuestions(	BasicDBList questions) {
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
