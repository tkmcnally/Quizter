package utils;

public class WordMatcher {

    //Calculate character distance from original answer
    public static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();

        a = a.replaceAll("\\W", "");
        b = b.replaceAll("\\W", "");

        a = a.replace("_", "");
        b = b.replace("_", "");

        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    //If character distance from original <= 1 then the phrases match
    public static boolean doesMatch(String a, String b) {
        if (a != null && !a.isEmpty() && b != null && !b.isEmpty()) {
            if (distance(a, b) <= 1) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        String[] data = {"stallions", "stallion", "stalliond", "sunday", "rosettacode", "raisethysword"};
        for (int i = 0; i < data.length; i += 2)
            System.out.println("distance(" + data[i] + ", " + data[i + 1] + ") = " + distance(data[i], data[i + 1]));
    }


}
