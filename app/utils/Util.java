package utils;

public class Util {
	
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
