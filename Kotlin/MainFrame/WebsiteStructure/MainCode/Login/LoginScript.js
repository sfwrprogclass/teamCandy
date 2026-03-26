const REAL_USERNAME = "Movie";
const REAL_PASSWORD = "1234";

document.getElementById("loginForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const user = document.getElementById("username").value.trim();
    const pass = document.getElementById("password").value.trim();

    // Empty field check
    if (user === "" || pass === "") {
        alert("Please fill in both fields.");
        return;
    }

    // ADMIN LOGIN
    if (user === "admin" && pass === "admin123") {
        window.location.href = "../Admin/AdminMovie.html";
        return;
    }

    // LOGIN
    if (user === REAL_USERNAME && pass === REAL_PASSWORD) {
        window.location.href = "Website.veiw.html";
        return;
    }

    // Normal user login
    if (user === "user" && pass === "password") {
        window.location.href = "index.html";
        return;
    }

    // If nothing matched
    alert("Incorrect username or password.");
});
