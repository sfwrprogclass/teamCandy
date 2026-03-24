const REAL_USERNAME = "Movie";
const REAL_PASSWORD = "1234";

document.getElementById("loginForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const user = document.getElementById("username").value.trim();
    const pass = document.getElementById("password").value.trim();

    if (user === "" || pass === "") {
        alert("Please fill in both fields.");
        return;
    }

    if (user === REAL_USERNAME && pass === REAL_PASSWORD) {
        window.location.href = "Website.veiw.html";
    } else {
        alert("Incorrect username or password.");
    }
});
