package services;

import java.util.Comparator;
import java.lang.Integer;

import models.User;

public class ScoreComparator implements Comparator<User> {
    @Override
    public int compare(User a, User b) {
    	Integer id1 = Integer.parseInt(a.getScore());
	    Integer id2 = Integer.parseInt(b.getScore());

	    if (id1 == null) {
	        return id2 == null ? 0 : 1;
	    }
	    if (id2 == null) {
	        return -1;
	    }
	    return id2.compareTo(id1);
    }
}
