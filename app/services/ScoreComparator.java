package services;

import java.util.Comparator;

import models.User;

public class ScoreComparator implements Comparator<User> {
    @Override
    public int compare(User a, User b) {
        return java.lang.Integer.compare(java.lang.Integer.parseInt(a.getScore()), java.lang.Integer.parseInt(a.getScore()));
    }
}
