# Candy Theaters

A command-line application for scheduling movie showtimes at a theater.

## Prerequisites

- **Java Development Kit (JDK) 22 or later**
  - Download: https://www.oracle.com/java/technologies/downloads/
  - Verify your installation: `java -version`

## Deployment

### 1. Clone the repository

```bash
git clone <repository-url>
cd teamCandy
```

### 2. Build the distribution

**Windows:**
```powershell
.\gradlew.bat installDist
```

**Linux/Mac:**
```bash
./gradlew installDist
```

This produces a ready-to-run distribution at `build/install/movie-manager/`.

### 3. Run the application

**Windows:**
```powershell
& ".\build\install\movie-manager\bin\movie-manager.bat"
```

**Linux/Mac:**
```bash
./build/install/movie-manager/bin/movie-manager
```

## Distributing to another machine

Zip the output folder and copy it to the target machine (which only needs Java installed, not Gradle):

```bash
cd build/install
zip -r movie-manager.zip movie-manager/
```

On the target machine, unzip and run the appropriate script from the `bin/` folder.

## Usage

Once running, the application will prompt you interactively:

```
Welcome to Candy Theaters!

Would you like to schedule a showtime? (Y or N):
```

- Enter movie title, duration (in minutes), and start time in `yyyy-MM-dd HH:mm` format
- Repeat to schedule additional showtimes
- Enter `N` when done to print the full schedule

## Verification

After deploying, follow these steps to confirm the system is working correctly:

1. Start the application using the run command above
2. When prompted, enter `Y` to schedule a showtime
3. Enter the following values:
   - Movie title: `The Matrix`
   - Duration: `136`
   - Start time: `2026-03-10 18:00`
4. When asked to schedule another, enter `N`

**Expected output:**

```
Thank you!

Showtimes for Theater 1:
The Matrix - starts at 03/10/2026 06:00 PM, ends at 03/10/2026 08:16 PM
```

If you see the showtime listed with correct start and end times, the system is working correctly.
