const dateList = document.getElementById("dateList");
const showtimeList = document.getElementById("showtimeList");
const showtimeStatus = document.getElementById("showtimeStatus");
const selectedDateTitle = document.getElementById("selectedDateTitle");

const seatingSection = document.getElementById("seatingSection");
const seatingHeader = document.getElementById("seatingHeader");
const seatingChart = document.getElementById("seatingChart");

const reserveButton = document.getElementById("reserveButton");
const reserveMessage = document.getElementById("reserveMessage");

const selectionSummarySection = document.getElementById("selectionSummarySection");
const summaryMovieName = document.getElementById("summaryMovieName");
const summaryShowtime = document.getElementById("summaryShowtime");
const summaryTheatre = document.getElementById("summaryTheatre");
const summaryAuditorium = document.getElementById("summaryAuditorium");
const summarySeats = document.getElementById("summarySeats");
const summaryPrice = document.getElementById("summaryPrice");

const payButton = document.getElementById("payButton");
const paymentMessage = document.getElementById("paymentMessage");
const cardName = document.getElementById("cardName");
const cardNumber = document.getElementById("cardNumber");
const cardExpiry = document.getElementById("cardExpiry");
const cardCvv = document.getElementById("cardCvv");

const paymentDialog = document.getElementById("paymentDialog");
const dialogTitle = document.getElementById("dialogTitle");
const dialogMessage = document.getElementById("dialogMessage");
const dialogSpinner = document.getElementById("dialogSpinner");
const dialogActions = document.getElementById("dialogActions");
const backToPaymentButton = document.getElementById("backToPaymentButton");
const returnHomeButton = document.getElementById("returnHomeButton");

let currentSelectedSeats = [];
let currentShowtime = null;

async function getTheaterByAuditorium(auditoriumId) {
  try {
    const response = await fetch("/api/theatre/auditorium/" + auditoriumId);

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    const theater = await response.json();
    return theater;
  } catch (error) {
    console.error("Error fetching theater:", error);
  }
}

async function getTheaterNameByAuditorium(auditoriumId) {
    try {
        const theater = await getTheaterByAuditorium(auditoriumId);
        return theater ? theater.name : "Unknown Theater";
    } catch (error) {
        console.error("Error fetching theater name:", error);
        return "Unknown Theater";
    }
}

async function getAuditoriumInfo(auditoriumId) {
    try {
        const response = await fetch("/api/auditorium/" + auditoriumId);
        if (!response.ok) throw new Error("Failed to fetch auditorium");

        const auditorium = await response.json();
        const theaterName = await getTheaterNameByAuditorium(auditoriumId);

        return {
            number: auditorium.number,
            theaterName: theaterName
        };
    } catch (error) {
        console.error("Error fetching auditorium info:", error);
        return {
            number: auditoriumId,      // fallback
            theaterName: "Unknown Theater"
        };
    }
}

function getDateString(date) {
    let year = date.getFullYear();
    let month = date.getMonth() + 1;
    let day = date.getDate();

    if (month < 10) {
        month = "0" + month;
    }

    if (day < 10) {
        day = "0" + day;
    }

    return year + "-" + month + "-" + day;
}

function getDateLabel(date) {
    return date.toDateString();
}

function getTime(dateTime) {
    let d = new Date(dateTime);
    return d.toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
}

function getSeatLabel(row, number) {
    return String.fromCharCode(65 + row) + (number + 1);
}

function clearPaymentFields() {
    cardName.value = "";
    cardNumber.value = "";
    cardExpiry.value = "";
    cardCvv.value = "";
    paymentMessage.innerHTML = "";
}

function removeActiveDateButtons() {
    let buttons = document.getElementsByClassName("date-button");

    for (let i = 0; i < buttons.length; i++) {
        buttons[i].classList.remove("active");
    }
}

function removeActiveShowtimes() {
    let cards = document.getElementsByClassName("showtime-item");

    for (let i = 0; i < cards.length; i++) {
        cards[i].classList.remove("active");
    }
}

function countAvailableSeats(chart) {
    let count = 0;

    for (let i = 0; i < chart.length; i++) {
        for (let j = 0; j < chart[i].length; j++) {
            if (chart[i][j].isReserved == false) {
                count++;
            }
        }
    }

    return count;
}

function updateReserveMessage() {
    if (currentSelectedSeats.length == 0) {
        reserveMessage.innerHTML = "";
    } else {
        reserveMessage.innerHTML = currentSelectedSeats.length + " seat(s) selected.";
    }
}

async function showSelectionSummary() {
    if (currentShowtime == null || currentSelectedSeats.length == 0) {
        selectionSummarySection.classList.add("hidden");
        return;
    }

    let seatLabels = currentSelectedSeats.map(s => getSeatLabel(s.row, s.number));
    let unitPrice = currentShowtime.unitPrice;
    let totalPrice = unitPrice * currentSelectedSeats.length;

    summaryMovieName.innerHTML = movieName;
    summaryShowtime.innerHTML = getTime(currentShowtime.startTime);

    const { number: auditoriumNumber, theaterName } = await getAuditoriumInfo(currentShowtime.auditoriumId);
    summaryAuditorium.innerHTML = auditoriumNumber;
    summaryTheatre.innerHTML = theaterName;

    summarySeats.innerHTML = seatLabels.join(", ");
    summaryPrice.innerHTML =
        currentSelectedSeats.length + " × $" + unitPrice.toFixed(2) + " = $" + totalPrice.toFixed(2);

    clearPaymentFields();
    selectionSummarySection.classList.remove("hidden");

    window.scrollTo({
        top: document.body.scrollHeight,
        behavior: "smooth"
    });
}

async function showSeating(showtime, clickedCard) {
    removeActiveShowtimes();
        clickedCard.classList.add("active");

        currentShowtime = showtime;
        currentSelectedSeats = [];
        reserveMessage.innerHTML = "";
        seatingChart.innerHTML = "";
        clearPaymentFields();
        selectionSummarySection.classList.add("hidden");
        seatingSection.classList.remove("hidden");

        const { number: auditoriumNumber, theaterName } = await getAuditoriumInfo(showtime.auditoriumId);

        seatingHeader.innerHTML =
            `Showtime: ${getTime(showtime.startTime)} | ${theaterName} | Auditorium ${auditoriumNumber}`;


    // Render seats
    for (let i = 0; i < showtime.seatingChart.length; i++) {
        let rowDiv = document.createElement("div");
        rowDiv.className = "seat-row";

        let rowLabel = document.createElement("div");
        rowLabel.className = "row-label";
        rowLabel.innerHTML = "Row " + (i + 1);
        rowDiv.appendChild(rowLabel);

        for (let j = 0; j < showtime.seatingChart[i].length; j++) {
            let seat = showtime.seatingChart[i][j];

            let seatButton = document.createElement("button");
            seatButton.className = "seat";
            seatButton.innerHTML = j + 1;

            if (seat.isReserved) {
                seatButton.classList.add("reserved");
                seatButton.disabled = true;
            } else {
                seatButton.classList.add("available");

                seatButton.onclick = function () {
                    let foundIndex = -1;

                    for (let k = 0; k < currentSelectedSeats.length; k++) {
                        if (currentSelectedSeats[k].row == i && currentSelectedSeats[k].number == j) {
                            foundIndex = k;
                            break;
                        }
                    }

                    if (foundIndex >= 0) {
                        currentSelectedSeats.splice(foundIndex, 1);
                        seatButton.classList.remove("selected");
                    } else {
                        currentSelectedSeats.push({
                            row: i,
                            number: j,
                            button: seatButton
                        });
                        seatButton.classList.add("selected");
                    }

                    selectionSummarySection.classList.add("hidden");
                    updateReserveMessage();
                };
            }

            rowDiv.appendChild(seatButton);
        }

        seatingChart.appendChild(rowDiv);
    }
}

async function loadShowtimes(dateString, dateLabel) {
    showtimeList.innerHTML = "";
    seatingChart.innerHTML = "";

    seatingSection.classList.add("hidden");
    selectionSummarySection.classList.add("hidden");

    selectedDateTitle.innerHTML = "Showtimes for " + dateLabel;
    showtimeStatus.innerHTML = "Loading showtimes...";
    showtimeStatus.style.display = "block";

    reserveMessage.innerHTML = "";
    currentSelectedSeats = [];
    currentShowtime = null;
    clearPaymentFields();

    try {
        let response = await fetch("/api/movies/" + movieId + "/showtimes?date=" + dateString);

        if (response.ok == false) {
            showtimeStatus.innerHTML = "Error loading showtimes.";
            return;
        }

        let showtimes = await response.json();

        if (showtimes.length == 0) {
            showtimeStatus.innerHTML = "No showtimes available for this date.";
            return;
        }

        showtimeStatus.style.display = "none";

        for (let i = 0; i < showtimes.length; i++) {
            let showtime = showtimes[i];

            let card = document.createElement("div");
            card.className = "showtime-item";

            let seatsAvailable = countAvailableSeats(showtime.seatingChart);

            let auditoriumNumber = showtime.auditoriumId; // fallback
            let theaterName = "Unknown Theater";

            try {
                const response = await fetch("/api/auditorium/" + showtime.auditoriumId);
                if (response.ok) {
                    const auditorium = await response.json();
                    auditoriumNumber = auditorium.number;

                    theaterName = await getTheaterNameByAuditorium(showtime.auditoriumId);
                }
            } catch (error) {
                console.error("Error fetching auditorium/theater info:", error);
            }

            card.innerHTML =
                "<div class='showtime-time'>" + getTime(showtime.startTime) + "</div>" +
                "<div class='showtime-meta'>" + theaterName + "</div>" +
                "<div class='showtime-meta'>" + " Auditorium " + auditoriumNumber +
                "<br>" + seatsAvailable + " seats available</div>";

            card.onclick = function () {
                showSeating(showtime, card);
            };

            showtimeList.appendChild(card);
        }
    } catch (error) {
        showtimeStatus.innerHTML = "Error loading showtimes.";
        console.log(error);
    }
}

function makeDateButtons() {
    let today = new Date();

    for (let i = 0; i < 7; i++) {
        let date = new Date();
        date.setDate(today.getDate() + i);

        let dateString = getDateString(date);
        let dateLabel = getDateLabel(date);

        let button = document.createElement("button");
        button.className = "date-button";
        button.innerHTML = dateLabel;

        if (i == 0) {
            button.classList.add("active");
            loadShowtimes(dateString, dateLabel);
        }

        button.onclick = function () {
            removeActiveDateButtons();
            button.classList.add("active");
            loadShowtimes(dateString, dateLabel);
        };

        dateList.appendChild(button);
    }
}

function showProcessingDialog() {
    paymentDialog.classList.remove("hidden");

    dialogTitle.innerHTML = "Processing Payment";
    dialogMessage.innerHTML = "Please wait while we process your payment.";

    dialogSpinner.classList.remove("hidden");
    dialogActions.classList.add("hidden");
    backToPaymentButton.classList.add("hidden");
    returnHomeButton.classList.add("hidden");
}

function showFailedDialog() {
    dialogTitle.innerHTML = "Payment Failed";
    dialogMessage.innerHTML = "Payment information did not match our records.";

    dialogSpinner.classList.add("hidden");
    dialogActions.classList.remove("hidden");
    backToPaymentButton.classList.remove("hidden");
    returnHomeButton.classList.add("hidden");
}

function showSuccessDialog() {
    dialogTitle.innerHTML = "Payment Successful";
    dialogMessage.innerHTML = "Your seats have been reserved successfully.";

    dialogSpinner.classList.add("hidden");
    dialogActions.classList.remove("hidden");
    backToPaymentButton.classList.add("hidden");
    returnHomeButton.classList.remove("hidden");
}

function waitFiveSeconds() {
    return new Promise(function (resolve) {
        setTimeout(resolve, 5000);
    });
}

reserveButton.onclick = function () {
    if (currentShowtime == null) {
        reserveMessage.innerHTML = "Please select a showtime first.";
        return;
    }

    if (currentSelectedSeats.length == 0) {
        reserveMessage.innerHTML = "Please select at least one seat.";
        return;
    }

    showSelectionSummary();
    reserveMessage.innerHTML = "Selection details shown below.";
};

payButton.onclick = async function () {
    if (currentShowtime == null) {
        paymentMessage.innerHTML = "Please select a showtime first.";
        return;
    }

    if (currentSelectedSeats.length == 0) {
        paymentMessage.innerHTML = "Please select seats first.";
        return;
    }

    if (cardName.value.trim() === "") {
        paymentMessage.innerHTML = "Please enter name on card.";
        return;
    }

    if (cardNumber.value.trim() === "") {
        paymentMessage.innerHTML = "Please enter card number.";
        return;
    }

    if (cardExpiry.value.trim() === "") {
        paymentMessage.innerHTML = "Please enter expiry date.";
        return;
    }

    if (cardCvv.value.trim() === "") {
        paymentMessage.innerHTML = "Please enter CVV.";
        return;
    }

    let seatsToReserve = [];

    for (let i = 0; i < currentSelectedSeats.length; i++) {
        seatsToReserve.push({
            row: currentSelectedSeats[i].row,
            number: currentSelectedSeats[i].number,
            isReserved: false
        });
    }

    let requestBody = {
        nameOnCard: cardName.value.trim(),
        cardNumber: cardNumber.value.trim(),
        expiry: cardExpiry.value.trim(),
        cvv: cardCvv.value.trim(),
        seats: seatsToReserve
    };

    showProcessingDialog();

    await waitFiveSeconds();

    try {
        let response = await fetch("/api/showtimes/" + currentShowtime.id + "/pay-and-reserve", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok == false) {
            showFailedDialog();
            return;
        }

        for (let i = 0; i < currentSelectedSeats.length; i++) {
            let selectedSeat = currentSelectedSeats[i];

            currentShowtime.seatingChart[selectedSeat.row][selectedSeat.number].isReserved = true;

            selectedSeat.button.classList.remove("selected");
            selectedSeat.button.classList.remove("available");
            selectedSeat.button.classList.add("reserved");
            selectedSeat.button.disabled = true;
        }

        paymentMessage.innerHTML = "Payment successful. Seats reserved.";
        reserveMessage.innerHTML = "";
        currentSelectedSeats = [];

        showSuccessDialog();
    } catch (error) {
        console.log(error);
        showFailedDialog();
    }
};

backToPaymentButton.onclick = function () {
    paymentDialog.classList.add("hidden");
};

returnHomeButton.onclick = function () {
    window.location.href = "/";
};

clearPaymentFields();
makeDateButtons();