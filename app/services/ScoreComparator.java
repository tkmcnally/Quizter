package services;

import java.util.Comparator;
import java.lang.Integer;

import models.QuizterUser;

public class ScoreComparator implements Comparator<QuizterUser> {
    @Override
    public int compare(QuizterUser a, QuizterUser b) {
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
