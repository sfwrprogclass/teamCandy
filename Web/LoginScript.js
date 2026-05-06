document.addEventListener("DOMContentLoaded", () => {
   console.log("Login script loaded");

   const REAL_USERNAME = "Movie";
   const REAL_PASSWORD = "1234";

   const form = document.getElementById("loginForm");

   form.addEventListener("submit", function(e) {
       e.preventDefault();

       const user = document.getElementById("username").value.trim();
       const pass = document.getElementById("password").value.trim();

       if (!user || !pass) {
           alert("Please fill in both fields.");
           return;
       }

       if (user === "admin" && pass === "admin123") {
           window.location.href = "AdminControl.html";
           return;
       }

       if (user === REAL_USERNAME && pass === REAL_PASSWORD) {
           window.location.href = "Website.view.html";
           return;
       }

       if (user === "user" && pass === "password") {
           window.location.href = "Website.view.html";
           return;
       }

       alert("Incorrect username or password.");
   });
});
