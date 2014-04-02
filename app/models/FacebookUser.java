package models;

import com.restfb.Facebook;
import com.restfb.types.Photo;


/**
 * Data Model representing a Facebook QuizterUser.
 * @author Thomas McNally
 *
 */
public class FacebookUser {

  @Facebook
  FacebookData data;
  
  @Facebook
  String id;
  
  @Facebook 
  String name;

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public String getId() {
	return id;
}

public void setId(String id) {
	this.id = id;
}

public FacebookData getData() {
	return data;
}

public void setData(FacebookData data) {
	this.data = data;
}

public void combine(FacebookUser user) {
	if(user.data != null) {
		this.data = user.data;
	}
	
	if(user.id != null) {
		this.id = user.id;
	}
	
	if(user.name != null) {
		this.name = user.name;
	}
}

}