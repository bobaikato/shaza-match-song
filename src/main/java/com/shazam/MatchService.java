package com.shazam;

import static java.util.Collections.reverseOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

public class MatchService {

  /** Represents a Map of Rating and related Songs from Highest rating to the least. */
  private final Map<Float, Set<Song>> ratingAndSongsMap = new TreeMap<>(reverseOrder());

  private static MatchService process(final Song song) {
    final MatchService ms = new MatchService();
    if (Objects.nonNull(song)) {
      ms.populateRatingAndSongsMap(song);
    } else {
      FeignedLogger.log("Supplied root Song is <null>.");
    }
    return ms;
  }

  /*
   * @param song Current song.
   * @param numTopRatedSimilarSongs the maximum number of song matches to return
   * @return List of top rated similar songs
   */
  public static List<Song> getSongMatches(final Song song, final int numTopRatedSimilarSongs) {
    final List<Song> songMatches = new ArrayList<>();

    // No point processing to return nothing.
    if (numTopRatedSimilarSongs > 0) {

      final MatchService ms = MatchService.process(song);
      final Collection<Set<Song>> songsCollection = ms.ratingAndSongsMap.values();

      for (final Set<Song> songs : songsCollection) {
        songs.remove(song); // O(1) operation to remove original song if it exist.
        if (songMatches.addAll(songs) && songMatches.size() >= numTopRatedSimilarSongs) {
          return songMatches.subList(0, numTopRatedSimilarSongs);
        }
      }
    } else {
      FeignedLogger.log("Skip processing: number of matched song requested is less than 1.");
    }

    FeignedLogger.log(songMatches.size() + " match(s) found.");

    /*
     * I considered returning an Empty list. But, null is returned because it's expected in the
     * Program class; with this final representation: result <null>
     *
     * Null also means there is not match.
     */
    return songMatches.isEmpty() ? null : songMatches;
  }

  /**
   * This method take the root song, and map the similar song by the song ratings.
   *
   * @param rootSong instance of a Song. Represent a songs with other similarities
   */
  private void populateRatingAndSongsMap(final Song rootSong) {
    FeignedLogger.log("Start populating rating and song map from Root song", rootSong);

    final List<Song> similarSongsReference = new ArrayList<>();
    similarSongsReference.add(rootSong); // initial/First song on the list.

    final Consumer<Song> populateRatingAndSongsMap =
        song -> {
          final Float songRating = song.getRating();
          if (this.ratingAndSongsMap.containsKey(songRating)) {
            this.ratingAndSongsMap.get(songRating).add(song);
          } else {
            final Set<Song> songs = new HashSet<>();
            songs.add(song);
            this.ratingAndSongsMap.put(songRating, songs);
          }
        };

    int currentIndex = 0;

    while (currentIndex < similarSongsReference.size()) {

      final Song currentSong = similarSongsReference.get(currentIndex++);

      currentSong.getSimilarSongs().parallelStream()
          .forEachOrdered(
              song -> {
                if (Objects.nonNull(song)) {
                  populateRatingAndSongsMap.accept(song);

                  if (!similarSongsReference.contains(song)) {
                    similarSongsReference.add(song);
                  }
                }
              });
    }

    FeignedLogger.log(
        "Finish populating Rating & Songs map. "
            + this.ratingAndSongsMap.size()
            + " unique ratings found.",
        rootSong);
  }
}
