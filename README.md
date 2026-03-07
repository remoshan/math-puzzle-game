# Math Puzzle Game

**Desktop game application built with Java (Swing) + FlatLaf, featuring authentication, difficulty modes, leaderboard, theming, and persistent local storage (SQLite).**

This project is a modern **Java desktop** app that uses **Swing** for UI, **FlatLaf** for Apple-inspired styling, and **SQLite** for local persistence. Users can **register/login**, select a **difficulty**, play the game, and view results on a **leaderboard**.

---

## Visuals

Add screenshots here (recommended):

```md
![Login](docs/images/login.png)
![Home](docs/images/home.png)
![Game](docs/images/game.png)
![Leaderboard](docs/images/leaderboard.png)
```

---

## Features

- **Authentication**: Register + login required before accessing the game
- **Local persistence (SQLite)**:
  - Stores users (with hashed passwords)
  - Stores scores and timing data
- **Difficulty system**:
  - Beginner (4×4)
  - Intermediate (6×6)
  - Advanced (8×8)
- **Leaderboard**: Displays top results
- **Theme system**: Light/Dark mode toggle using FlatLaf
- **Music**:
  - Background music starts when the game starts
  - Stops when the game ends
  - Mute/unmute + volume control
- **Async operations** (no UI freezing):
  - Music playback runs off the UI thread
  - Image fetching/loading runs off the UI thread

---

## Tech Stack

- **Language**: Java
- **UI**: Swing + FlatLaf
- **Build Tool**: Maven
- **Database**: SQLite (`sqlite-jdbc`)

---

## Getting Started

### Prerequisites

- **Java**: 17+ recommended
- **Maven**: 3.9+ recommended

> If you see `JAVA_HOME environment variable is not defined correctly`, set `JAVA_HOME` to your JDK installation path and reopen your terminal/IDE.

### Installation

Clone the repository and build:

```bash
git clone <your-repo-url>
cd "Math Puzzle Game"
mvn clean package
```

### Run

Run from Maven (requires an exec plugin if you add it) or run directly from your IDE.

**IDE run**: run the main class:

- `com.mathpuzzlegame.Main`

If you want to run from command line without adding extra plugins:

```bash
java -cp target/classes com.mathpuzzlegame.Main
```

---

## Usage

- **Register / Login**
  - First-time users: create an account
  - Returning users: login
- **Home**
  - Start a new game
  - Change difficulty
  - View leaderboard
  - Toggle Light/Dark theme
- **Game**
  - Play using the selected difficulty rules
  - Use hints (amount varies by difficulty)
  - Control music (mute + volume)
- **Game Over**
  - See final score and best score
  - Restart, change difficulty, or return home

---

## Project Structure

```text
src/main/java/com/mathpuzzlegame
  Main.java
  data/
    DatabaseManager.java
  model/
    Difficulty.java
    User.java
    UserScore.java
    GameRecord.java
  net/
    BananaApi.java
    MusicApi.java
  service/
    AuthService.java
    GameService.java
    ImageService.java
    MusicService.java
  theme/
    ThemeManager.java
  ui/
    AppFrame.java
    LoginView.java
    RegisterView.java
    HomeView.java
    DifficultyView.java
    GameView.java
    GameOverView.java
    LeaderboardView.java
```

---

## Configuration

### Database

SQLite database is created automatically on first run.

- Default DB file: `math_puzzle.db`
- Configured in: `com.mathpuzzlegame.Main` via JDBC URL: `jdbc:sqlite:math_puzzle.db`

### External API Notes

- **Banana API**: Used for card images. If the API does not return reachable image URLs in your environment, the game falls back to generated placeholder card images.
- **Music API**: Music is streamed from configured public sample WAV URLs; playback depends on network/DNS access to those hosts.

---

## Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch from `main`
3. Commit changes with clear messages
4. Open a Pull Request with:
   - Summary of changes
   - Test plan / steps to verify

---

## License

This project is licensed under the **MIT License** (add a `LICENSE` file if not present).

