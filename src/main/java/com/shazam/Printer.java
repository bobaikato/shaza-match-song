package com.shazam;

import java.util.Objects;

/**
 * This is not a real logger.
 * All it does is Print stuff out. Created Just for the purpose of this test.
 */
public final class FeignedLogger {


  public static void log(String msg, final Song song) {
    String songName = "<unavailable>";
    String songRating = "<unavailable>";

    if (Objects.nonNull(song)) {
      songName = song.getName();
      songRating = String.valueOf(song.getRating());
    }

    System.out.println(msg + "\n\n— SONG META —\nSong Name: " + songName + "\nRating: " + songRating + "\n");
  }

  public static void log(String msg) {
    System.out.println(msg + "\n");
  }
}
