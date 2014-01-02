package models;

import com.restfb.Facebook;
import com.restfb.types.Photo;

public class FacebookUser {

  // By default, assumes JSON attribute name is the same as the Java field name  

  @Facebook
  Data data;
  
  @Facebook
  String id;

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

}