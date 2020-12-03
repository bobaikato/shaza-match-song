package com.shazam;

import java.util.Objects;

/**
 * This is not a real logger,It just something to simplify always calling System print within the
 * code it just prints stuff out, nothing serious.
 */
public final class FeignedLogger {

  public static void log(String msg, final Song song) {

    String songName = "<unavailable>";
    String songRating = "<unavailable>";

    if (Objects.nonNull(song)) {
      songName = song.getName();
      songRating = String.valueOf(song.getRating());
    }

    System.out.println(
        msg + "\n\n— SONG META —\nSong Name: " + songName + "\nRating: " + songRating + "\n");
  }

  public static void log(String msg) {
    System.out.println(msg + "\n");
  }
}
