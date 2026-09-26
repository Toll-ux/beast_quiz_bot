package domain;

public class User {
    private final int id;
    private String username;
    private String surname;
    private int highestScore;

    public User(int id) {
        this.id = id;
        this.username = "";
        this.surname = "";
        this.highestScore = 0;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSurname() {
        return surname;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void updateHighestScore(int score) {
        this.highestScore = Math.max(this.highestScore, score);
    }

}