package models;

import com.restfb.Facebook;
import com.restfb.types.Photo;

public class FacebookUser {

  // By default, assumes JSON attribute name is the same as the Java field name  

  @Facebook
  Data data;
  
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

public Data getData() {
	return data;
}

public void setData(Data data) {
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