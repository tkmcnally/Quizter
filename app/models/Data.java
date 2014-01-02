package models;

import com.restfb.Facebook;

public class Data {

	@Facebook
	String url;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Facebook
	int height;
	
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Facebook
	int width;
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Facebook
	boolean is_silhouette;

	public boolean isIs_silhouette() {
		return is_silhouette;
	}

	public void setIs_silhouette(boolean is_silhouette) {
		this.is_silhouette = is_silhouette;
	}
	
	
}
