import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Program {

    public static void main(String[] args) throws IOException {
        final Map<String, Song> songMap = new HashMap<>();
        Song rootSong = null;
        int numTopRatedSimilarSongs = 0;

        final Scanner in = new Scanner(System.in);
        in.useLocale(new Locale("en", "US"));

        while (in.hasNextLine()) {
            final String type = in.next();

            if (type.equals("song")) {
                final String name = in.next();
                final float rating = in.nextFloat();
                songMap.put(name, new Song(name, rating));
            } else if (type.equals("similar")) {
                final Song song1 = songMap.get(in.next());
                final Song song2 = songMap.get(in.next());
                song1.addSimilarSong(song2);
            } else if (type.equals("getSongMatches")) {
                rootSong = songMap.get(in.next());
                numTopRatedSimilarSongs = in.nextInt();
                break;
            } else {
                // ignore
            }
        }

        final List<Song> result = MatchService.getSongMatches(rootSong, numTopRatedSimilarSongs);

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
