package model;

public class UserInfo {
    private String username;
    private double score;
    private int wins;

    public UserInfo(String username, double score, int wins) {
        this.username = username;
        this.score = score;
        this.wins = wins;
    }

    // Getters
    public String getUsername() { return username; }
    public double getScore() { return score; }
    public int getWins() { return wins; }

    @Override
    public String toString() {
        return String.format("%s - Điểm: %.2f, Thắng: %d", username, score, wins);
    }
}