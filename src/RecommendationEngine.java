import java.util.ArrayList;
import java.util.HashMap;

// Builds recommendation lists with genre filter and sort modes.
public class RecommendationEngine {
    public static final String MODE_RATING_DESC = "rating_desc";
    public static final String MODE_RATING_ASC = "rating_asc";
    public static final String MODE_YEAR_DESC = "year_desc";
    public static final String MODE_YEAR_ASC = "year_asc";
    public static final String MODE_RANDOM = "random";

    // Collect distinct genres from all movies, sorted alphabetically.
    public ArrayList<String> listGenres(MovieLibrary library) {
        ArrayList<String> genres = new ArrayList<String>();
        ArrayList<Movie> movies = library.getAllMovies();
        for (int i = 0; i < movies.size(); i++) {
            String genre = movies.get(i).getGenre();
            if (!containsIgnoreCase(genres, genre)) {
                genres.add(genre);
            }
        }
        sortStrings(genres);
        return genres;
    }

    // Main entry: filter by genre (or ALL), exclude watched/watchlist, then sort.
    public ArrayList<Movie> recommend(User user, MovieLibrary library, String genreFilter, String sortMode, int n) {
        if (n <= 0) {
            n = 5;
        }
        if (n > 10) {
            n = 10;
        }
        String mode;
        if (sortMode == null) {
            mode = MODE_RATING_DESC;
        } else {
            mode = sortMode;
        }

        boolean allGenres = genreFilter == null || genreFilter.length() == 0;
        ArrayList<Movie> candidates = new ArrayList<Movie>();
        ArrayList<Movie> all = library.getAllMovies();
        for (int i = 0; i < all.size(); i++) {
            Movie movie = all.get(i);
            if (!allGenres && !movie.getGenre().equalsIgnoreCase(genreFilter)) {
                continue;
            }
            if (!isExcluded(user, movie.getId())) {
                candidates.add(movie);
            }
        }

        if (candidates.isEmpty()) {
            return candidates;
        }

        sortMovies(candidates, mode);

        ArrayList<Movie> result = new ArrayList<Movie>();
        for (int i = 0; i < candidates.size() && result.size() < n; i++) {
            result.add(candidates.get(i));
        }
        return result;
    }

    // Selection sort on movies using the chosen comparison.
    private void sortMovies(ArrayList<Movie> movies, String mode) {
        for (int i = 0; i < movies.size(); i++) {
            int targetIndex = i;
            for (int j = i + 1; j < movies.size(); j++) {
                if (better(movies.get(j), movies.get(targetIndex), mode)) {
                    targetIndex = j;
                }
            }
            Movie temp = movies.get(i);
            movies.set(i, movies.get(targetIndex));
            movies.set(targetIndex, temp);
        }

        if (mode.equals(MODE_RANDOM)) {
            shuffleMovies(movies);
        }
    }

    // Decide if "current" should come before "target" under the given mode.
    private boolean better(Movie current, Movie target, String mode) {
        if (mode.equals(MODE_RATING_ASC)) {
            return current.getRating() < target.getRating();
        }
        if (mode.equals(MODE_YEAR_DESC)) {
            if (current.getYear() > target.getYear()) {
                return true;
            }
            if (current.getYear() == target.getYear()) {
                return current.getRating() > target.getRating();
            }
            return false;
        }
        if (mode.equals(MODE_YEAR_ASC)) {
            if (current.getYear() < target.getYear()) {
                return true;
            }
            if (current.getYear() == target.getYear()) {
                return current.getRating() > target.getRating();
            }
            return false;
        }
        if (mode.equals(MODE_RANDOM)) {
            return false;
        }
        return current.getRating() > target.getRating();
    }

    // Fisher-Yates shuffle for random order.
    private void shuffleMovies(ArrayList<Movie> movies) {
        for (int i = movies.size() - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            Movie temp = movies.get(i);
            movies.set(i, movies.get(j));
            movies.set(j, temp);
        }
    }

    // Selection sort for strings (case-insensitive).
    private void sortStrings(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            int best = i;
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).compareToIgnoreCase(list.get(best)) < 0) {
                    best = j;
                }
            }
            String temp = list.get(i);
            list.set(i, list.get(best));
            list.set(best, temp);
        }
    }

    private boolean containsIgnoreCase(ArrayList<String> list, String text) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcluded(User user, String movieId) {
        String id = movieId.toUpperCase();
        if (user.getWatchlist().contains(id)) {
            return true;
        }
        if (user.getHistory().containsMovie(id)) {
            return true;
        }
        return false;
    }
}
