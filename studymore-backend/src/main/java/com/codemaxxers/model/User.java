package com.codemaxxers.model;

import java.util.List;

public class User {

    private Long userId;
    private String username;
    private String email;
    private String passwordHash;
    // private Rank rank;
    private int rating;
    private int coinBalance;
    private int studyStreak;
    private long totalStudyTime;
    private long dailyStudyTime;
    // private MascotCat mascotCat;
    // private Inventory inventory;
    private List<User> friends;
    // private List<Task> tasks;

    public User(Long userId) {
        // TODO (write the fetch logic from backend -- or cache or local database -- and initlize this user object that fetched user)
    }

    // Constructing a new user
    public User(String username, String email, String passwordHash) {
        // this.userId = SnowflakeIDGenerator.generate();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        // this.rank = Rank.BRONZE;
        this.rating = 0;
        this.coinBalance = 0;
        this.studyStreak = 0;
        this.totalStudyTime = 0L;
        this.dailyStudyTime = 0L;
        // this.mascotCat = new MascotCat();
        // this.inventory = new Inventory();
        this.friends = new java.util.ArrayList<>();
        // this.tasks = new java.util.ArrayList<>();
    }

    public void login() {
        // TODO
    }

    public void register() {
        // TODO
    }

    public void updateProfile() {
        // TODO
    }

    //public Stats getStats() {
    //    // TODO
    //    return null;
    //}

}