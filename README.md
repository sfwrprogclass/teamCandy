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

Once running, you can choose between the **Backend (Employee)** and **Customer Website** by running different main classes or interacting with the menu in `Main.kt`.

### Backend Mode (Employee)
Run `edu.teamcandy.MainKt`. The backend allows employees to:
- **Schedule Showtimes:** Select a movie and enter a start time.
- **Sell Tickets:** Select a showtime and a seat (e.g., A1) to sell a ticket in-person.

### Customer Website
Run `edu.teamcandy.CustomerWebsiteKt`. The website allows customers to:
- **View Schedule:** See all upcoming showtimes across all theaters.
- **Search Movies:** Search by **Title**, **Cast**, or **Genre**.
- **Book Tickets:** Select a movie, a showtime, and a seat to purchase a ticket online.

## Movie Database

The movie database is managed in `src/main/kotlin/repositories/MovieRepository.kt`. Each movie now includes:
- **Title and Duration**
- **Cast List**
- **Genres**
- **Description**

## Seating and Theaters

- Each theater (managed in `Theater.kt`) has a default seating capacity (e.g., 5 rows, 10 seats per row).
- Every showtime has its own unique seating chart.
- Seats are identified by a Row (A-E) and Number (1-10), e.g., `A1`.
- Occupied seats are marked with `X`, while available seats are marked with `.`.

## Verification

1. Run the Backend (`Main.kt`).
2. Select `1` to schedule a showtime.
3. Choose `The Matrix` and enter `2026-03-10 18:00`.
4. Select `2` to sell a ticket.
5. Choose the Matrix showtime and enter seat `A1`.
6. Confirm the "Ticket sold successfully" message appears.
