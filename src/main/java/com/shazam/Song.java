package com.shazam;

import java.util.ArrayList;
import java.util.List;

public class Song {
  private final String name;
  private final float rating;
  private final List<Song> similarSongs = new ArrayList<>();

  public Song(String name, float rating) {
    this.name = name;
    this.rating = rating;
  }

  public String getName() {
    return name;
  }

  public float getRating() {
    return rating;
  }

  public void addSimilarSong(Song song) {
    similarSongs.add(song);
    song.similarSongs.add(this);
  }

  public List<Song> getSimilarSongs() {
    return similarSongs;
  }

  public boolean similarSongsIsNotEmpty() {
    return !this.similarSongs.isEmpty();
  }
}