import java.util.ArrayList;

// Stores all watched records for one user.
public class HistoryLog {
    private final ArrayList<History> entries;// List of watch history entries

    public HistoryLog() {
        entries = new ArrayList<>();//Creates an empty HistoryLog
    }

    public HistoryLog(ArrayList<History> existing) {
        entries = existing;
    }

    public void addEntry(String movieId, String date) {
        String id = movieId.toUpperCase();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getMovieId().equalsIgnoreCase(id)) {
                entries.set(i, new History(id, date));// Update existing entry if movie was already watched
                return;
            }
        }
        // Add new entry if movie hasn't been watched before
        entries.add(new History(id, date));
    }
    //Checks if the user has watched a specific movie
    public boolean containsMovie(String movieId) {
        String id = movieId.toUpperCase();
        for (History entry : entries) {
            if (entry.getMovieId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }
    //Gets all history entries
    public ArrayList<History> getEntries() {
        return entries;
    }

    public String toStorageString() {
        if (entries.isEmpty()) {
            return "";
        }
        String result = entries.get(0).toStorageString();
        for (int i = 1; i < entries.size(); i++) {
            result = result + ";" + entries.get(i).toStorageString();
        }
        return result;
    }
}
