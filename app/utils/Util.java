package utils;

import java.util.*;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import models.QuizterUser;

/**
 * Utility class for common operations.
 * @author Thomas McNally
 *
 */
public class Util {
	
	
	/**
	 * Data Model representing a Facebook QuizterUser.
	 * @param density - Double representing the screen density of the Android phone.
	 * @return pixel_size - The appropriate pixel size to be used to display the 
	 * profile picture on that density.
	 */
	public static int getPictureSize(String density) {
		double int_density = Double.parseDouble(density);
		int pixel_size = 100;
		
		if(int_density == 0.75) {
			pixel_size = 75;
		} else if(int_density == 0.75) {
			pixel_size = 100;
		} else if(int_density == 1.0) {
			pixel_size = 150;
		} else if(int_density == 1.5) {
			pixel_size = 225;
		} else if(int_density == 2.0) {
			pixel_size = 300;
		} else if(int_density == 3.0) {
			pixel_size = 300;
		} else if(int_density == 4.0) {
			pixel_size = 300;
		}
		
		return pixel_size;
	}
	
	public static BasicDBList hashQuestions(BasicDBList node) {
		
		Set<String> set = node.keySet();
		for(String s: set) {
			BasicDBObject obj = (BasicDBObject) node.get(s);	
			
			String Q = obj.getString("question");
			String A = obj.getString("answer");
			String hash = (Q + A).hashCode() + "";

			obj.put("q_id", hash);
						
		}
	
		return node;
		
	}
	
	public static boolean hasSetupProfile(QuizterUser user) {
		boolean hasSetup = true;
		for(Object obj: user.getQuestions()) {
			LinkedHashMap qa = (LinkedHashMap) obj;
			if(Constants.DEFAULT_QUESTION.equals(qa.get("question"))) {
				return false;
			}
		}
		
		return hasSetup;
	}


    public static <E> Collection<E> makeCollection(Iterable<E> iter) {
        Collection<E> list = new ArrayList<E>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }
}
