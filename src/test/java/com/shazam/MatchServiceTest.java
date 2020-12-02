package com.shazam;

import static java.lang.Float.parseFloat;
import static java.nio.file.Files.readAllLines;
import static java.util.Collections.sort;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import com.honerfor.cutils.function.Idler;
import com.honerfor.cutils.value.Try;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    final String dataFilePath = "src/test/java/com/shazam/input-data.txt";
    Try.of(() -> readAllLines(Paths.get(dataFilePath)))
        .onSuccessOrElse(
            lines -> {
              lines.parallelStream().forEachOrdered(populateSongMap);
            },
            ex -> {
              System.out.println("An error occurred loading data => " + ex);
            });
  }

  @Test
  public void test() {
    final List<Song> result = MatchService.getSongMatches(songMap.get("F"), 1);

    String output = "result";

    if (result == null) {
      output += " <null>";
    } else {
      sort(result, Comparator.comparing(s -> s.getName()));

      for (Song m : result) {
        output += " ";
        output += m.getName();
      }
    }

    System.out.println(output);
  }

  private static Stream<Arguments> resources$A() {
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
        Arguments.of("J", 4, Arrays.asList("G", "H", "I", "F")));
  }

  @DisplayName("[A] Should take valid Song Root and Song Match count.")
  @ParameterizedTest(name = "{index} => Root Song={0}, Match Count={1}, Expected Result={2}")
  @MethodSource("resources$A")
  void getSongMatchesWithValidRootSongAndMatchCount(
      final String rootSongKey, final int topRatedSimilarSongsCount, List<String> songKeys) {

    final List<Song> actualSongList = Idler.supply(() -> songKeys
        .parallelStream()
        .map(songMap::get)
        .collect(Collectors.toList()))
        .get();

    final Song rootSong = songMap.get(rootSongKey);
    final List<Song> expectedSongList = MatchService.getSongMatches(rootSong, topRatedSimilarSongsCount);

    assert expectedSongList != null;

    Assertions.assertTrue(actualSongList.containsAll(expectedSongList));
    Assertions.assertTrue(expectedSongList.containsAll(actualSongList));
    Assertions.assertEquals(expectedSongList.size(), actualSongList.size());
  }
}
