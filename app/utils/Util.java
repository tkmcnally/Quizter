package utils;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for common operations.
 * @author Thomas McNally
 *
 */
public class Util {
	
	
	/**
	 * Data Model representing a Facebook User.
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
	
	public static ObjectNode hashQuestions(JsonNode node) {
		Iterator<JsonNode> iter = node.elements();

		ObjectNode o = JsonNodeFactory.instance.objectNode();
		ArrayNode a = o.putArray("questions");
		while(iter.hasNext()) {
			JsonNode n = iter.next();
			String Q = n.get("question").textValue();
			String A = n.get("answer").textValue();
			String hash = (Q + A).hashCode() + "";

			ObjectNode obj = JsonNodeFactory.instance.objectNode();
			obj.put("question", Q);
			obj.put("answer", A);
			obj.put("q_id", hash);
			
			a.add(obj);
		}
		return o;
		
	}

}
