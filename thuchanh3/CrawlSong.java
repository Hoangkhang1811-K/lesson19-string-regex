package thuchanh3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlSong {
    public static void main(String[] args) {
        try {
            String content = Files.readString(Paths.get("E:\\codegemModule2\\lesson19-string-regex\\lesson19-string-regex\\thuchanh3\\songs.html"), StandardCharsets.UTF_8);


            content = content.replaceAll("\\R+", "");


            Pattern pattern = Pattern.compile(
                    "<a\\s+class\\s*=\\s*\"name_song\"[^>]*>(.*?)</a>",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                System.out.println(matcher.group(1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
