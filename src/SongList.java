import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SongList {

    public static ArrayList<String> getTitles(String csvFilePath) {
        ArrayList<String> titles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstRow = true;

            while ((line = br.readLine()) != null) {
                if (firstRow) {
                    firstRow = false; // skip header row
                    continue;
                }
                String[] columns = line.split(",");
                titles.add(columns[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return titles;
    }
}
