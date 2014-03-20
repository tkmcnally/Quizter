package services;

import java.util.Comparator;
import java.lang.Integer;

import models.User;

public class ScoreComparator implements Comparator<User> {
    @Override
    public int compare(User a, User b) {
        return Integer.compare(Integer.parseInt(a.getScore()), Integer.parseInt(a.getScore()));
    }
}
