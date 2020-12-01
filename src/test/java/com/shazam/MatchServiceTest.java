package com.shazam;

import static java.lang.Float.parseFloat;
import static java.nio.file.Files.readAllLines;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import com.honerfor.cutils.value.Try;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    final String filePath = "src/test/java/com/shazam/input-data.txt";

    Try.of(() -> readAllLines(Paths.get(filePath)))
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
    final List<Song> result = MatchService.getSongMatches(null, 6);

    String output = "result";

    if (result == null) {
      output += " <null>";
    } else {
      Collections.sort(result, Comparator.comparing(s -> s.getName()));

      for (Song m : result) {
        output += " ";
        output += m.getName();
      }
    }

    System.out.println(output);
  }
}
