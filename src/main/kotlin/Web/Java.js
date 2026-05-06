// ===============================
// GLOBAL DARK MODE TOGGLE
// ===============================
document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.getElementById("darkModeToggle");

    if (!toggle) return;

    // Load saved mode
    if (localStorage.getItem("darkMode") === "enabled") {
        document.body.classList.add("dark-mode");
        toggle.textContent = "Light Mode";
    }

    toggle.addEventListener("click", () => {
        document.body.classList.toggle("dark-mode");

        const isDark = document.body.classList.contains("dark-mode");
        toggle.textContent = isDark ? "Light Mode" : "Dark Mode";

        localStorage.setItem("darkMode", isDark ? "enabled" : "disabled");
    });
});


// ===============================
// DROPDOWN LOGIC
// ===============================
document.querySelectorAll(".dropdown").forEach((btn, index) => {
    btn.addEventListener("click", () => {
        const content = document.querySelectorAll(".dropdown-content")[index];
        content.style.display = content.style.display === "block" ? "none" : "block";
    });
});


// ===============================
// LOAD MOVIES ON MAIN WEBSITE
// ===============================
fetch("http://localhost:9090/api/movies")
    .then(response => response.json())
    .then(movies => {
        const container = document.getElementById("movie-list");

        movies.forEach(movie => {
            const section = document.createElement("section");
            section.style.padding = "40px";
            section.style.textAlign = "center";

            // ⭐ FALLBACK IMAGE LOGIC
            const imgSrc =
                movie.imageUrl && movie.imageUrl.trim() !== ""
                    ? movie.imageUrl
                    : "Image/Movie.png";

            section.innerHTML = `
                <div class="movie-card" data-movie="${movie.id}">
                    <img src="${imgSrc}" class="movie-poster">
                    <h2>${movie.name}</h2>
                    <p><strong>Duration:</strong> ${movie.durationMinutes} minutes</p>
                    <p><strong>Rating:</strong> ${movie.rating}</p>
                    <p>${movie.description}</p>
                </div>
            `;

            container.appendChild(section);
        });

        // Click to open movie details page
        document.querySelectorAll('.movie-card').forEach(card => {
            card.addEventListener('click', () => {
                const movieId = card.getAttribute('data-movie');
                window.location.href = `Movie-info.html?id=${movieId}`;
            });
        });
    })
    .catch(err => console.error("Failed to load movies:", err));
