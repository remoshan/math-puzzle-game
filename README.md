# Math Puzzle Game

**Desktop game application built with Java (Swing) + FlatLaf, featuring authentication, difficulty modes, leaderboard, theming, and persistent local storage (SQLite).**

This project is a modern **Java desktop** app that uses **Swing** for UI, **FlatLaf** for Apple-inspired styling, and **SQLite** for local persistence. Users can **register/login**, select a **difficulty**, play the game, and view results on a **leaderboard**.

---

## Visuals

<img width="1109" height="742" alt="Screenshot 2026-03-11 151510" src="https://github.com/user-attachments/assets/336576d2-1dbb-4247-ba10-074b644b507a" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151522" src="https://github.com/user-attachments/assets/1ebc9f9f-5196-4769-9874-f259ff45e3a5" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151902" src="https://github.com/user-attachments/assets/530614e8-c3a9-4d84-9949-c1bf316666d1" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151558" src="https://github.com/user-attachments/assets/3a1a8c1a-4bec-4603-8361-7b1f41f30cc4" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151625" src="https://github.com/user-attachments/assets/aaa9961e-9d29-4cac-af32-9368e6167ed3" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151656" src="https://github.com/user-attachments/assets/37c8ada8-bde0-4bce-8558-3f0ffdfe107e" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151708" src="https://github.com/user-attachments/assets/51f4d688-0c8f-4850-886c-dcbccb8989c5" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151721" src="https://github.com/user-attachments/assets/74f14f12-51b9-44ac-91b3-57cf64a9a4f3" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151750" src="https://github.com/user-attachments/assets/71ca8315-3fe1-4c28-a87e-bca12b4898b7" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151756" src="https://github.com/user-attachments/assets/19d767be-6fbf-4cdd-ac08-0700233b759a" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151807" src="https://github.com/user-attachments/assets/b7f75cd2-cd22-4bc1-afce-62b781c88977" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151837" src="https://github.com/user-attachments/assets/57d35b83-bc24-424a-be07-c5cee26f978c" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151822" src="https://github.com/user-attachments/assets/d300efb3-a165-42b9-b35c-07440c3a1267" />
<img width="1109" height="742" alt="Screenshot 2026-03-11 151843" src="https://github.com/user-attachments/assets/26437b33-7181-488f-9ccb-a752b8e37beb" />

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
git clone github.com/remoshan/math-puzzle-game
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

This project is licensed under the **MIT License**. The `LICENSE` file is included in the repository root; see it for the full license text and details.

