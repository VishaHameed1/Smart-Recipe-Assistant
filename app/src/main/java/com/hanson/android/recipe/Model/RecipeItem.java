package com.hanson.android.recipe.Model;

import java.io.Serializable;

/**
 * Updated by Gemini on 2026-01-03.
 * Full Recipe Item Model with standardized getters to fix "symbol not found" errors.
 */
public class RecipeItem implements Serializable {
    private int id;
    private String category;
    private String recipeName;
    private String author;
    private String uploadDate;
    private String howTo;
    private String description;
    private byte[] thumbnail;
    private byte[] mainImg;
    private int score;
    private int likeCount;

    // Full Constructor
    public RecipeItem(int id, String category, String recipeName, String author, String uploadDate,
                      String howTo, String description, byte[] thumbnail, byte[] mainImg, int score) {
        this.id = id;
        this.category = category;
        this.recipeName = recipeName;
        this.author = author;
        this.uploadDate = uploadDate;
        this.howTo = howTo;
        this.description = description;
        this.thumbnail = thumbnail;
        this.mainImg = mainImg;
        this.score = score;

    }

    // --- GETTERS (Standardized to match your Activity calls) ---

    public int get_id() {
        return id;
    }

    public void set_id(int id) {
        this.id = id;
    }

    // Fix: Added get_category() to resolve the error in RecipeActivity.java
    public String get_category() {
        return category;
    }

    public void set_category(String category) {
        this.category = category;
    }

    // Keeping this for compatibility with other adapters if needed
    public String get_recipeCategory() {
        return category;
    }

    public String get_recipeName() {
        return recipeName;
    }

    public void set_recipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String get_author() {
        return author;
    }

    public String get_uploadDate() {
        return uploadDate;
    }

    public String get_howTo() {
        return howTo;
    }

    public void set_howTo(String howTo) {
        this.howTo = howTo;
    }

    public String get_description() {
        return description;
    }

    // --- SETTERS ---

    public byte[] get_thumbnail() {
        return thumbnail;
    }

    public void set_thumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public byte[] get_mainImg() {
        return mainImg;
    }

    public int get_score() {
        return score;
    }

    public void set_score(int score) {
        this.score = score;
    }

    public int get_recipeScore() {
        return score;
    }

    // FIX: Added for "get_likeCount" error
    public int get_likeCount() {
        return likeCount;
    }

    public void set_likeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}