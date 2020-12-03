package com.shazam;

import static java.lang.Float.parseFloat;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Song Match Service Test")
class MatchServiceTest {

  private static final Map<String, Song> songMap = new HashMap<>();

  @BeforeAll
  public static void setup() {
    final Consumer<String> populateSongMap =
        line -> {
          if (isNotBlank(line)) {
            final String[] lineArray = line.split(" ");

            if (lineArray[0].trim().equalsIgnoreCase("song")) {
              final String name = lineArray[1];
              final float rating = parseFloat(lineArray[2]);
              songMap.put(name, new Song(name, rating));
            } else if (lineArray[0].trim().equalsIgnoreCase("similar")) {
              final Song song1 = songMap.get(lineArray[1]);
              final Song song2 = songMap.get(lineArray[2]);
              song1.addSimilarSong(song2);
            }
          }
        };


    try {
      final String dataFilePath = "src/test/java/com/shazam/input-data.txt";
      Files.readAllLines(Paths.get(dataFilePath))
          .parallelStream()
          .forEachOrdered(populateSongMap);

    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private static Stream<Arguments> resources$A() {
    return Stream.of(
        Arguments.of("D", -1, null),
        Arguments.of("A", 0, null),
        Arguments.of("K", 3, null),
        Arguments.of("G", -4, null),
        Arguments.of("X", 4, null),
        Arguments.of("A1", 1, null),
        Arguments.of("A1", 1, null),
        Arguments.of("B0", 0, null),
        Arguments.of("P", 3, null),
        Arguments.of("N", 10, null));
  }

  @DisplayName("[A] Should return null if invalid Song Root and/or Song Match count is provided.")
  @ParameterizedTest(name = "{index} => Root Song={0}, Match Count={1}, Expected Result={2}")
  @MethodSource("resources$A")
  void getSongMatchesWithInvalidRootSongAndMatchCount(
      final String rootSongKey, final int topRatedSimilarSongsCount, final List<Song> expectedSongList) {

    final Song rootSong = songMap.get(rootSongKey);
    final List<Song> actualSongList = MatchService.getSongMatches(rootSong, topRatedSimilarSongsCount);

    Assertions.assertEquals(expectedSongList, actualSongList);
    Assertions.assertNull(actualSongList);
  }

  private static Stream<Arguments> resources$B() {
    return Stream.of(
        Arguments.of("A", 1, Collections.singletonList("D")),
        Arguments.of("A", 2, Arrays.asList("B", "D")),
        Arguments.of("A", 4, Arrays.asList("C", "D", "B")),
        Arguments.of("B", 1, Collections.singletonList("D")),
        Arguments.of("B", 2, Arrays.asList("D", "C")),
        Arguments.of("B", 4, Arrays.asList("D", "C", "A")),
        Arguments.of("C", 1, Collections.singletonList("D")),
        Arguments.of("C", 2, Arrays.asList("B", "D")),
        Arguments.of("C", 4, Arrays.asList("A", "D", "B")),
        Arguments.of("D", 1, Collections.singletonList("B")),
        Arguments.of("D", 2, Arrays.asList("B", "C")),
        Arguments.of("D", 4, Arrays.asList("C", "A", "B")),
        Arguments.of("F", 4, Arrays.asList("G", "H", "I", "J")),
        Arguments.of("G", 4, Arrays.asList("F", "J", "I", "H")),
        Arguments.of("J", 4, Arrays.asList("G", "H", "I", "F")),
        Arguments.of("J", 4, Arrays.asList("G", "H", "I", "F")),
        Arguments.of("B100", 1, Collections.singletonList("B11158")),
        Arguments.of("B0", 5, Arrays.asList("B10979", "B11009", "B11103", "B11158", "B11170")));
  }

  @DisplayName("[B] Should take valid Song Root and Song Match count.")
  @ParameterizedTest(name = "{index} => Root Song={0}, Match Count={1}, Expected Result={2}")
  @MethodSource("resources$B")
  void getSongMatchesWithValidRootSongAndMatchCount(
      final String rootSongKey, final int topRatedSimilarSongsCount, final List<String> songKeys) {

    final List<Song> expectedSongList = songKeys
        .parallelStream()
        .map(songMap::get)
        .collect(Collectors.toList());

    final Song rootSong = songMap.get(rootSongKey);
    final List<Song> actualSongList = MatchService.getSongMatches(rootSong, topRatedSimilarSongsCount);

    assert actualSongList != null;

    Assertions.assertTrue(actualSongList.containsAll(expectedSongList));
    Assertions.assertTrue(expectedSongList.containsAll(actualSongList));
    Assertions.assertEquals(expectedSongList.size(), actualSongList.size());
  }


  @Test
  @DisplayName("[C] Should return null if Song Root Reference itself alone.")
  void songRootSelfReference() {
    final List<Song> actualSongList = MatchService.getSongMatches(songMap.get("X"), 2);
    Assertions.assertNull(actualSongList);
  }

  @Test
  @DisplayName("[D] Should successfully operate as normal within a concurrent setup.")
  void explicitConcurrentProcessing() throws ExecutionException {
    final ExecutorService es = Executors.newCachedThreadPool();
    final List<Callable<Map<String, List<Song>>>> actualSongMatchesCallables = new ArrayList<>();
    final Map<String, List<Song>> expectedSongsMap = new HashMap<>();

    final BiConsumer<String, List<String>> populateExpectedSongsMap = (rootSongKey, songKeys) -> {
      final List<Song> expectedSongList = songKeys
          .parallelStream()
          .map(songMap::get)
          .collect(Collectors.toList());

      expectedSongsMap.put(rootSongKey, expectedSongList);
    };

    resources$B()
        .parallel()
        .forEachOrdered(resource -> {
          final String songKey = String.valueOf(resource.get()[0]);

          if (!expectedSongsMap.containsKey(songKey)) {
            final int matchCount = (int) resource.get()[1];
            final List<String> expectedSongsKeys = (List<String>) resource.get()[2];

            populateExpectedSongsMap.accept(songKey, expectedSongsKeys);

            final Callable<Map<String, List<Song>>> getSongMatchesCallable = () -> {
              final Map<String, List<Song>> songsAndRootSongKeyMap = new HashMap<>();
              final List<Song> actualSongList = MatchService.getSongMatches(songMap.get(songKey), matchCount);
              songsAndRootSongKeyMap.put(songKey, actualSongList);
              return songsAndRootSongKeyMap;
            };

            actualSongMatchesCallables.add(getSongMatchesCallable);
          }
        });

    try {
      for (final Future<Map<String, List<Song>>> result : es.invokeAll(actualSongMatchesCallables)) {
        final Map.Entry<String, List<Song>> entry = result.get().entrySet().iterator().next();
        final String songKey = entry.getKey();
        final List<Song> actualSongList = entry.getValue();

        final List<Song> expectedSongList = expectedSongsMap.get(songKey);

        Assertions.assertTrue(actualSongList.containsAll(expectedSongList));
        Assertions.assertTrue(expectedSongList.containsAll(actualSongList));
        Assertions.assertEquals(expectedSongList.size(), actualSongList.size());
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}