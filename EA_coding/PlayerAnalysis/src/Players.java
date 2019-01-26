import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Players {
    public static void main(String[] args) {
        try {
            InputStream fileInputStream;
            BufferedReader bufferedReader;
            final String fileName = "/Users/mukesh/Documents/workspace/PlayerAnalysis/players_log.txt";
            final String outputFile = "/Users/mukesh/Documents/workspace/PlayerAnalysis/players_log_output.txt";
            final String DATA_SEPARATOR = "\t";

            Map<String, Map<String, Integer>> playerLogMap = new HashMap<>();

            try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
                stream.forEach(line -> {
                    String[] tokens = line.split("\\t");
                    Map<String, Integer> gameTimeStMap = new HashMap<>();
                    gameTimeStMap.put(tokens[1], Integer.valueOf(tokens[2]));
                    if (playerLogMap.get(tokens[0]) == null) {
                        playerLogMap.put(tokens[0], gameTimeStMap);
                    } else {
                        Map<String, Integer> tempMap = playerLogMap.get(tokens[0]);
                        tempMap.put(tokens[1], Integer.valueOf(tokens[2]));
                        playerLogMap.put(tokens[0], tempMap);
                    }

                });
            }


            playerLogMap.entrySet().parallelStream().forEach(entry -> {
                playerLogMap.put(entry.getKey(), sortByValue(entry.getValue()));
            });

            Map<String, Integer> tripletMap = new HashMap<>();
            playerLogMap.entrySet().parallelStream().forEach(entry -> {

                StringBuffer sBuf = new StringBuffer();
                entry.getValue().entrySet().forEach(inEntry -> {
                    sBuf.append(inEntry.getValue());
                });

                String gameTimeSt = sBuf.toString();


                List<String> triplet = new ArrayList<>();
                for (int i = 0; i < gameTimeSt.length(); i++) {
                    if (i + 3 <= gameTimeSt.length()) {
                        String tripletStr = gameTimeSt.substring(i, i + 3);
                        if (tripletMap.get(tripletStr) == null) {
                            tripletMap.put(tripletStr, 1);
                        } else {
                            tripletMap.put(tripletStr, 1 + tripletMap.get(tripletStr));
                        }
                    } else break;

                }


            });

            System.out.println("tripletMap " + tripletMap);

            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(outputFile)), StandardCharsets.UTF_8));
            tripletMap.forEach((key, value) -> {
                try {
                    writer.write(key + DATA_SEPARATOR + value + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Map<String, Integer> sortByValue(final Map<String, Integer> playerRecord) {
        return playerRecord.entrySet()
                .parallelStream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

}