package utils.comparators;

import models.QuizterUser;

import java.util.Comparator;

//Custom comparator for sorting Collection of User's by their score
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
