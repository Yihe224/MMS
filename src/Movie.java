// Represents one movie record loaded from CSV.
public class Movie {
    private final String id;// Unique identifier for the movie
    private final String title;// Movie title
    private final String genre;// Movie genre (e.g., Action, Comedy)
    private final int year;// Release year
    private final double rating;// User/ critic rating
    //Constructor to create a Movie object with full metadata
    public Movie(String id, String title, String genre, int year, double rating) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
    }
    // Getter methods to get ID, Title, Genre, Year, Rating
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    public double getRating() {
        return rating;
    }

    // Compact output used in menus.
    public String shortDescription() {
        return id + " - " + title + " (" + genre + ", " + year + ") rating: " + rating;
    }
}
