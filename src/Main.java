import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

// CLI entry point for the movie tracker.
public class Main {
    private static final String MOVIE_FILE = "data/movies.csv"; // File path for movie data storage (CSV format)
    private static final String USER_FILE = "data/users.csv"; // File path for account data storage (CSV format)

    public static void main(String[] args) {
        MovieLibrary movieLibrary = new MovieLibrary(); // Initialize movie library and load data from file
        if (!movieLibrary.loadFromFile(MOVIE_FILE)) {
            return; // Exit if movie data fails to load
        }

        UserStorage userStorage = new UserStorage();// Initialize user storage and load existing user accounts
        HashMap<String, User> users = userStorage.loadUsers(USER_FILE);
        // Initialize recommendation engine for personalized suggestions
        RecommendationEngine recommendationEngine = new RecommendationEngine();
        Scanner scanner = new Scanner(System.in);

        User currentUser = null;
        boolean running = true;// Controls the main application loop
        while (running) { // Show guest menu if no user is logged in
            if (currentUser == null) {
                showGuestMenu();
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1" -> currentUser = handleLogin(scanner, users);
                    case "2" -> createAccount(scanner, users, userStorage);
                    case "3" -> running = false;// Exit application
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } else {
                // Show authenticated user menu when logged in
                showUserMenu(currentUser);
                String choice = scanner.nextLine().trim();   //trim avoid space
                switch (choice) {
                    case "1" -> browseMovies(movieLibrary, currentUser);
                    case "2" -> {
                        addMovieToWatchlist(scanner, currentUser, movieLibrary);
                        userStorage.saveUsers(users, USER_FILE);// Persist updated user data
                    }
                    case "3" -> {
                        removeMovieFromWatchlist(scanner, currentUser, movieLibrary);
                        userStorage.saveUsers(users, USER_FILE);
                    }
                    case "4" -> viewWatchlist(currentUser, movieLibrary);

                    case "5" -> {
                        markMovieAsWatched(scanner, currentUser, movieLibrary);
                        userStorage.saveUsers(users, USER_FILE);
                    }
                    case "6" -> viewHistory(currentUser, movieLibrary);
                    case "7" -> getRecommendations(scanner, currentUser, movieLibrary, recommendationEngine);
                    case "8" -> {
                        changePassword(scanner, currentUser);
                        userStorage.saveUsers(users, USER_FILE);
                    }
                    case "9" -> {
                        currentUser = null;
                        System.out.println("Logged out.");
                    }
                    case "10" -> running = false;
                    default -> System.out.println("Invalid option. Please try again.");
                }
            }
        }

        userStorage.saveUsers(users, USER_FILE);   //backup
        scanner.close();
        System.out.println("Goodbye!");
    }
    //Displays the main menu for guest (unauthenticated) users

    private static void showGuestMenu() {
        System.out.println("\n--- Movie Tracker ---");
        System.out.println("1. Login");
        System.out.println("2. Create account");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    private static void showUserMenu(User user) {
        System.out.println("\n--- Welcome, " + user.getUsername() + " ---");
        System.out.println("1. Browse movies");
        System.out.println("2. Add movie to watchlist");
        System.out.println("3. Remove movie from watchlist");
        System.out.println("4. View watchlist");
        System.out.println("5. Mark movie as watched");
        System.out.println("6. View history");
        System.out.println("7. Get recommendations");
        System.out.println("8. Change password");
        System.out.println("9. Logout");
        System.out.println("10. Exit");
        System.out.print("Choose an option: ");
    }

    // Simple username/password login from users.csv.
    private static User handleLogin(Scanner scanner, HashMap<String, User> users) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (!users.containsKey(username)) {
            System.out.println("User not found.");
            return null;
        }
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        User user = users.get(username);
        if (user.verifyPassword(password)) {
            System.out.println("Login successful!");
            return user;
        }
        System.out.println("Wrong password.");
        return null;
    }

    // Create new account with password confirmation and length check.
    private static void createAccount(Scanner scanner, HashMap<String, User> users, UserStorage storage) {
        System.out.println("\n--- Create Account ---");
        System.out.print("Choose a username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (username.length() < 3 || username.length() > 14) {
            System.out.println("Username length must be between 3 and 14 characters.");
            return;
        }
        if (users.containsKey(username)) {
            System.out.println("Username already exists.");
            return;
        }
        System.out.print("Choose a password (5-14 characters): ");
        String password = scanner.nextLine().trim();
        if (password.length() < 5 || password.length() > 14) {
            System.out.println("Password length must be between 5 and 14 characters.");
            return;
        }
        System.out.print("Confirm password: ");
        String confirm = scanner.nextLine().trim();
        if (!password.equals(confirm)) {
            System.out.println("Passwords do not match.");
            return;
        }
        User newUser = new User(username, password, new Watchlist(), new HistoryLog());
        users.put(username, newUser);
        storage.saveUsers(users, USER_FILE);
        System.out.println("Account created. You can now log in.");
    }

    // List all movies with a short description.
    private static void browseMovies(MovieLibrary library, User currentUser) {
        System.out.println("\n--- All Movies ---");
        ArrayList<Movie> movies = library.getAllMovies();
        for (Movie movie : movies) {
            String status = "";
            if (currentUser != null && currentUser.hasWatched(movie.getId())) {
                status = "[watched] ";
            }
            System.out.println(status + movie.shortDescription());
        }
    }

    private static void addMovieToWatchlist(Scanner scanner, User user, MovieLibrary library) {
        System.out.print("Enter movie ID to add: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Movie movie = library.getMovieById(id);
        if (movie == null) {
            System.out.println("Movie not found.");
            return;
        }

        // If already in watchlist, just report it and stop.
        if (user.getWatchlist().contains(id)) {
            System.out.println(movie.getTitle() + " (" + movie.getYear() + ") is already in your watchlist.");
            return;
        }

        if (user.hasWatched(id)) {
            System.out.println("You have watched this movie before.");
            while (true) {
                System.out.print("Add to watchlist anyway? (y/n): ");
                String answer = scanner.nextLine().trim().toLowerCase();
                if (answer.equals("n")) {
                    return;
                }
                if (answer.equals("y")) {
                    break;
                }
                System.out.println("Please enter y or n.");
            }
        }
        user.addToWatchlist(id);
        System.out.println(movie.getTitle() + " (" + movie.getYear() + ") is added to your watchlist.");
    }

    private static void removeMovieFromWatchlist(Scanner scanner, User user, MovieLibrary library) {
        if (user.getWatchlist().getItems().isEmpty()) {
            System.out.println("Watchlist is empty.");
            return;
        }
        System.out.print("Enter movie ID to remove: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Movie movie = library.getMovieById(id);
        if (user.removeFromWatchlist(id)) {
            if (movie != null) {
                System.out.println(movie.getTitle() + " (" + movie.getYear() + ") is removed from your watchlist.");
            } else {
                System.out.println(id + " is removed from your watchlist.");
            }
        } else {
            System.out.println("That movie is not in your watchlist.");
        }
    }

    private static void viewWatchlist(User user, MovieLibrary library) {
        ArrayList<String> items = user.getWatchlist().getItems();
        if (items.isEmpty()) {
            System.out.println("Watchlist is empty.");
            return;
        }
        System.out.println("\n--- Your Watchlist ---");
        for (String id : items) {
            Movie movie = library.getMovieById(id);
            if (movie != null) {
                System.out.println(movie.shortDescription());
            } else {
                System.out.println(id);
            }
        }
    }

    // Mark watched with today's date and auto-remove from watchlist.
    private static void markMovieAsWatched(Scanner scanner, User user, MovieLibrary library) {
        System.out.print("Enter movie ID watched: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Movie movie = library.getMovieById(id);
        if (movie == null) {
            System.out.println("Movie not found.");
            return;
        }
        if (user.hasWatched(id)) {
            System.out.println("You have watched this movie before. Date will be updated.");
        }
        String date = LocalDate.now().toString();
        user.markWatched(id, date);
        System.out.println("Marked " + movie.getTitle() + " (" + movie.getYear() + ") as watched on " + date + ".");
    }

    private static void viewHistory(User user, MovieLibrary library) {
        ArrayList<History> entries = user.getHistory().getEntries();
        if (entries.isEmpty()) {
            System.out.println("History is empty.");
            return;
        }
        System.out.println("\n--- Viewing History ---");
        for (History entry : entries) {
            Movie movie = library.getMovieById(entry.getMovieId());
            if (movie == null) {
                System.out.println(entry.getMovieId() + " on " + entry.getWatchedDate());
            } else {
                System.out.println(movie.shortDescription() + " on " + entry.getWatchedDate());
            }
        }
    }

    // Interactive recommendation flow: choose genre, sort mode, count.
    private static void getRecommendations(Scanner scanner, User user, MovieLibrary library, RecommendationEngine engine) {
        ArrayList<String> genres = engine.listGenres(library);
        System.out.println("\n--- Choose Genre ---");
        for (int i = 0; i < genres.size(); i++) {
            System.out.println((i + 1) + ". " + genres.get(i));
        }
        System.out.println((genres.size() + 1) + ". All");

        String genreFilter;
        while (true) {
            System.out.print("Enter choice: ");
            String genreChoiceText = scanner.nextLine().trim();
            try {
                int genreChoice = Integer.parseInt(genreChoiceText);
                if (genreChoice >= 1 && genreChoice <= genres.size()) {
                    genreFilter = genres.get(genreChoice - 1);
                    break;
                }
                if (genreChoice == genres.size() + 1) {
                    genreFilter = null; // All
                    break;
                }
                System.out.println("Please enter a number between 1 and " + (genres.size() + 1) + ".");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }

        }

        System.out.println("\n--- Sort Options ---");
        System.out.println("1. Rating high to low");
        System.out.println("2. Rating low to high");
        System.out.println("3. Year new to old");
        System.out.println("4. Year old to new");
        System.out.println("5. Random");

        int sortChoice;
        while (true) {

            System.out.print("Enter choice: ");
            String sortChoiceText = scanner.nextLine().trim();
            try {
                sortChoice = Integer.parseInt(sortChoiceText);
                if (sortChoice >= 1 && sortChoice <= 5) {
                    break;
                } else {
                    System.out.println("Please enter a number between 1 and 5.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }

        }
        String sortMode = chooseSortMode(sortChoice);

        int number;
        while (true) {
            System.out.print("How many recommendations? (max 10): ");
            String text = scanner.nextLine().trim();

            try {
                number = Integer.parseInt(text);
                if (number >= 1 && number <= 10) {
                    break;
                } else {
                    System.out.println("Please enter a number between 1 and 10.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        ArrayList<Movie> recs = engine.recommend(user, library, genreFilter, sortMode, number);
        if (recs.isEmpty()) {
            System.out.println("No recommendations available.");
            return;
        }
        System.out.println("\n--- Recommendations ---");

        for (int i = 0; i < recs.size(); i++) {
            Movie movie = recs.get(i);
            System.out.println((i + 1) + ". " + movie.shortDescription());
        }
        System.out.println("Found " + recs.size() + " matching item(s).");
    }

    private static void changePassword(Scanner scanner, User user) {
        System.out.print("Enter current password: ");
        String current = scanner.nextLine().trim();
        if (!user.verifyPassword(current)) {
            System.out.println("Current password incorrect.");
            return;
        }
        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine().trim();
        if (newPass.length() < 5 || newPass.length() > 14) {
            System.out.println("Password length must be between 5 and 14 characters.");
            return;
        }
        System.out.print("Confirm new password: ");
        String confirm = scanner.nextLine().trim();
        if (!newPass.equals(confirm)) {
            System.out.println("Passwords do not match.");
            return;
        }
        user.changePassword(newPass);
        System.out.println("Password updated.");
    }

    private static String chooseSortMode(int choice) {
        return switch (choice) {
            case 2 -> RecommendationEngine.MODE_RATING_ASC;
            case 3 -> RecommendationEngine.MODE_YEAR_DESC;
            case 4 -> RecommendationEngine.MODE_YEAR_ASC;
            case 5 -> RecommendationEngine.MODE_RANDOM;
            default -> RecommendationEngine.MODE_RATING_DESC;
        };
    }
}
