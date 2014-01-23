package utils;

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

}
