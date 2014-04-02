package models;

import com.restfb.Facebook;

public class FacebookData {

    /**
     * Data Model representing a Facebook QuizterUser's data.
     *
     * @author Thomas McNally
     */

    @Facebook
    String url;
    @Facebook
    int height;
    @Facebook
    int width;
    @Facebook
    boolean is_silhouette;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isIs_silhouette() {
        return is_silhouette;
    }

    public void setIs_silhouette(boolean is_silhouette) {
        this.is_silhouette = is_silhouette;
    }


}
