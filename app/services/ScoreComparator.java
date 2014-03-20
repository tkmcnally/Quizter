package services;

import java.util.Comparator;
import java.lang.Integer;

import models.User;

public class ScoreComparator implements Comparator<User> {
    @Override
    public int compare(User a, User b) {
    	int return_num = Integer.compare(Integer.parseInt(a.getScore()), Integer.parseInt(a.getScore()));
        return return_num;
    }
}
