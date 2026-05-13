(function () {
  "use strict";

  angular
    .module("skywaysApp", [])
    .directive("renderIcons", renderIcons)
    .controller("SkywaysController", SkywaysController);

  function renderIcons($timeout) {
    return {
      restrict: "A",
      link: function () {
        $timeout(function () {
          if (window.lucide) {
            window.lucide.createIcons();
          }
        });
      }
    };
  }

  SkywaysController.$inject = ["$http", "$timeout"];

  function SkywaysController($http, $timeout) {
    var vm = this;

    vm.config = {};
    vm.loading = {};
    vm.toast = {};
    vm.notice = "";
    vm.searchMode = "inventory";
    vm.userId = "asha@example.com";
    vm.inventoryFlights = [];
    vm.providerFlights = [];
    vm.bookings = [];
    vm.payments = [];
    vm.notifications = [];
    vm.currentBooking = null;
    vm.currentPayment = null;
    vm.gatewayOnline = null;
    vm.lastNotification = null;
    vm.selectedFlight = null;

    vm.search = {
      origin: "DEL",
      destination: "BOM",
      departureDate: addDays(new Date(), 7),
      passengers: 1
    };

    vm.booking = defaultBooking();
    vm.payment = defaultPayment();
    vm.notification = defaultNotification();

    vm.activeFlights = activeFlights;
    vm.addPassenger = addPassenger;
    vm.badgeClass = badgeClass;
    vm.cancelBooking = cancelBooking;
    vm.clearSelection = clearSelection;
    vm.confirmBooking = confirmBooking;
    vm.createBooking = createBooking;
    vm.createPayment = createPayment;
    vm.dateLabel = dateLabel;
    vm.dismissToast = dismissToast;
    vm.fillSamplePassenger = fillSamplePassenger;
    vm.flightDuration = flightDuration;
    vm.formatDateTime = formatDateTime;
    vm.gatewayClass = gatewayClass;
    vm.gatewayStatusText = gatewayStatusText;
    vm.money = money;
    vm.refreshUserData = refreshUserData;
    vm.removePassenger = removePassenger;
    vm.searchFlights = searchFlights;
    vm.seatTone = seatTone;
    vm.selectFlight = selectFlight;
    vm.sendNotification = sendNotification;
    vm.timeOnly = timeOnly;
    vm.useBooking = useBooking;
    vm.workflowClass = workflowClass;

    loadConfig();
    searchFlights();
    refreshUserData();

    function loadConfig() {
      $http.get("/config").then(function (response) {
        vm.config = response.data || {};
      });
    }

    function searchFlights() {
      vm.loading.search = true;
      vm.notice = "";
      var params = {
        origin: normalizeAirport(vm.search.origin),
        destination: normalizeAirport(vm.search.destination),
        departureDate: dateOnly(vm.search.departureDate),
        passengers: Number(vm.search.passengers || 1)
      };

      var inventoryRequest = $http.get("/api/flights/search", { params: params });
      var providerRequest = $http.get("/api/flights/provider-search", { params: params });

      return Promise.allSettled([inventoryRequest, providerRequest])
        .then(function (results) {
          $timeout(function () {
            var inventory = results[0];
            var provider = results[1];
            vm.gatewayOnline = inventory.status === "fulfilled" || provider.status === "fulfilled";

            if (inventory.status === "fulfilled") {
              vm.inventoryFlights = inventory.value.data || [];
            } else {
              vm.inventoryFlights = [];
              showError("Flight search failed", normalizeError(inventory.reason));
            }

            if (provider.status === "fulfilled") {
              vm.providerFlights = provider.value.data || [];
            } else {
              vm.providerFlights = [];
            }

            if (!vm.inventoryFlights.length && vm.providerFlights.length) {
              vm.notice = "No SkyWays inventory matched, but provider comparison results are available.";
            }

            vm.loading.search = false;
            renderLucide();
          });
        });
    }

    function selectFlight(flight) {
      vm.selectedFlight = flight;
      vm.booking.flightId = flight.flightNumber;
      vm.booking.totalAmount = Number(flight.baseFare || flight.fare || 0) * Number(vm.search.passengers || 1);
      vm.booking.currency = flight.currency || "INR";
      resizePassengers(Number(vm.search.passengers || 1));
      vm.booking.passengers.forEach(function (passenger) {
        passenger.email = passenger.email || vm.userId;
      });
      showSuccess("Flight selected", flight.flightNumber + " is ready for booking.");
      renderLucide();
    }

    function clearSelection() {
      vm.selectedFlight = null;
      vm.booking.flightId = "";
    }

    function createBooking() {
      vm.loading.booking = true;
      var payload = {
        userId: vm.userId,
        flightId: String(vm.booking.flightId || "").toUpperCase(),
        totalAmount: Number(vm.booking.totalAmount || 0),
        currency: String(vm.booking.currency || "INR").toUpperCase(),
        passengers: vm.booking.passengers.map(function (passenger) {
          return {
            firstName: passenger.firstName,
            lastName: passenger.lastName,
            dateOfBirth: dateOnly(passenger.dateOfBirth),
            passportNumber: String(passenger.passportNumber || "").toUpperCase(),
            email: passenger.email
          };
        })
      };

      $http.post("/api/bookings", payload)
        .then(function (response) {
          vm.gatewayOnline = true;
          useBooking(response.data);
          showSuccess("Booking created", "Booking " + response.data.id + " is now " + response.data.status + ".");
          return refreshUserData();
        })
        .catch(function (error) {
          updateGatewayFromError(error);
          showError("Booking failed", normalizeError(error));
        })
        .finally(function () {
          vm.loading.booking = false;
          renderLucide();
        });
    }

    function createPayment() {
      vm.loading.payment = true;
      var payload = {
        bookingId: vm.payment.bookingId,
        userId: vm.userId,
        amount: Number(vm.payment.amount || 0),
        currency: String(vm.payment.currency || "INR").toUpperCase(),
        paymentMethodToken: vm.payment.paymentMethodToken
      };

      $http.post("/api/payments", payload)
        .then(function (response) {
          vm.gatewayOnline = true;
          vm.currentPayment = response.data;
          showSuccess("Payment " + response.data.status.toLowerCase(), response.data.failureReason || "Payment reference " + response.data.providerReference + ".");
          return refreshUserData();
        })
        .catch(function (error) {
          updateGatewayFromError(error);
          showError("Payment failed", normalizeError(error));
        })
        .finally(function () {
          vm.loading.payment = false;
          renderLucide();
        });
    }

    function confirmBooking() {
      updateBookingStatus("confirm");
    }

    function cancelBooking() {
      updateBookingStatus("cancel");
    }

    function updateBookingStatus(action) {
      if (!vm.currentBooking || !vm.currentBooking.id) {
        return;
      }

      $http.patch("/api/bookings/" + vm.currentBooking.id + "/" + action)
        .then(function (response) {
          vm.gatewayOnline = true;
          useBooking(response.data);
          showSuccess("Booking updated", "Booking is now " + response.data.status + ".");
          return refreshUserData();
        })
        .catch(function (error) {
          updateGatewayFromError(error);
          showError("Status update failed", normalizeError(error));
        })
        .finally(renderLucide);
    }

    function sendNotification() {
      vm.loading.notification = true;
      var payload = {
        bookingId: vm.notification.bookingId,
        userId: vm.userId,
        recipient: vm.notification.recipient,
        subject: vm.notification.subject,
        body: vm.notification.body
      };

      $http.post("/api/notifications", payload)
        .then(function (response) {
          vm.gatewayOnline = true;
          vm.lastNotification = response.data;
          showSuccess("Notification " + response.data.status.toLowerCase(), "Sent to " + response.data.recipient + ".");
          return refreshUserData();
        })
        .catch(function (error) {
          updateGatewayFromError(error);
          showError("Notification failed", normalizeError(error));
        })
        .finally(function () {
          vm.loading.notification = false;
          renderLucide();
        });
    }

    function refreshUserData() {
      if (!vm.userId) {
        return Promise.resolve();
      }

      vm.loading.activity = true;
      var params = { userId: vm.userId };
      var bookings = $http.get("/api/bookings", { params: params });
      var payments = $http.get("/api/payments", { params: params });
      var notifications = $http.get("/api/notifications", { params: params });

      return Promise.allSettled([bookings, payments, notifications])
        .then(function (results) {
          $timeout(function () {
            vm.bookings = results[0].status === "fulfilled" ? results[0].value.data || [] : [];
            vm.payments = results[1].status === "fulfilled" ? results[1].value.data || [] : [];
            vm.notifications = results[2].status === "fulfilled" ? results[2].value.data || [] : [];
            vm.gatewayOnline = results.some(function (result) {
              return result.status === "fulfilled";
            }) || vm.gatewayOnline === true;
            vm.loading.activity = false;
            renderLucide();
          });
        });
    }

    function useBooking(booking) {
      vm.currentBooking = booking;
      vm.userId = booking.userId || vm.userId;
      vm.payment.bookingId = booking.id;
      vm.payment.amount = Number(booking.totalAmount || 0);
      vm.payment.currency = booking.currency || "INR";
      vm.notification.bookingId = booking.id;
      vm.notification.recipient = booking.userId || vm.userId;
      vm.notification.subject = "SkyWays booking " + String(booking.status || "update").toLowerCase();
      vm.notification.body = "Your SkyWays booking for flight " + booking.flightId + " is " + String(booking.status || "updated").toLowerCase() + ".";
    }

    function addPassenger() {
      if (vm.booking.passengers.length < 9) {
        vm.booking.passengers.push(emptyPassenger());
        recalculateTotal();
      }
      renderLucide();
    }

    function removePassenger() {
      if (vm.booking.passengers.length > 1) {
        vm.booking.passengers.pop();
        recalculateTotal();
      }
      renderLucide();
    }

    function fillSamplePassenger() {
      vm.userId = "asha@example.com";
      vm.booking.passengers = [samplePassenger(0)];
      vm.search.passengers = 1;
      if (!vm.booking.flightId) {
        vm.booking.flightId = "SK101";
      }
      if (!vm.booking.totalAmount) {
        vm.booking.totalAmount = 6500;
      }
      vm.booking.currency = "INR";
      showSuccess("Sample added", "Passenger details are ready to submit.");
      renderLucide();
    }

    function resizePassengers(count) {
      var safeCount = Math.max(1, Math.min(9, count));
      while (vm.booking.passengers.length < safeCount) {
        vm.booking.passengers.push(emptyPassenger());
      }
      while (vm.booking.passengers.length > safeCount) {
        vm.booking.passengers.pop();
      }
    }

    function recalculateTotal() {
      if (vm.selectedFlight) {
        vm.booking.totalAmount = Number(vm.selectedFlight.baseFare || vm.selectedFlight.fare || 0) * vm.booking.passengers.length;
      }
    }

    function activeFlights() {
      return vm.searchMode === "provider" ? vm.providerFlights : vm.inventoryFlights;
    }

    function defaultBooking() {
      return {
        flightId: "",
        totalAmount: 0,
        currency: "INR",
        passengers: [emptyPassenger()]
      };
    }

    function defaultPayment() {
      return {
        bookingId: "",
        amount: 0,
        currency: "INR",
        paymentMethodToken: "pm_success"
      };
    }

    function defaultNotification() {
      return {
        bookingId: "",
        recipient: "asha@example.com",
        subject: "SkyWays booking confirmed",
        body: "Your SkyWays booking has been confirmed."
      };
    }

    function emptyPassenger() {
      return {
        firstName: "",
        lastName: "",
        dateOfBirth: null,
        passportNumber: "",
        email: vm ? vm.userId : ""
      };
    }

    function samplePassenger(index) {
      return {
        firstName: index === 0 ? "Asha" : "Rohan",
        lastName: index === 0 ? "Rao" : "Mehta",
        dateOfBirth: new Date(index === 0 ? 1998 : 1996, index === 0 ? 2 : 6, index === 0 ? 15 : 22),
        passportNumber: index === 0 ? "A1234567" : "B7654321",
        email: vm.userId
      };
    }

    function normalizeAirport(value) {
      return String(value || "").trim().toUpperCase();
    }

    function dateOnly(value) {
      if (!value) {
        return "";
      }
      if (value instanceof Date) {
        var year = value.getFullYear();
        var month = String(value.getMonth() + 1).padStart(2, "0");
        var day = String(value.getDate()).padStart(2, "0");
        return year + "-" + month + "-" + day;
      }
      return String(value).slice(0, 10);
    }

    function addDays(date, days) {
      var copy = new Date(date);
      copy.setDate(copy.getDate() + days);
      return copy;
    }

    function formatDateTime(value) {
      if (!value) {
        return "Not scheduled";
      }
      return new Date(value).toLocaleString([], {
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      });
    }

    function dateLabel(value) {
      if (!value) {
        return "Choose date";
      }
      var date = value instanceof Date ? value : new Date(value);
      if (Number.isNaN(date.getTime())) {
        return String(value).slice(0, 10);
      }
      return date.toLocaleDateString([], {
        month: "short",
        day: "numeric",
        year: "numeric"
      });
    }

    function timeOnly(value) {
      if (!value) {
        return "--:--";
      }
      var date = value instanceof Date ? value : new Date(value);
      if (Number.isNaN(date.getTime())) {
        return "--:--";
      }
      return date.toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit"
      });
    }

    function flightDuration(flight) {
      if (!flight || !flight.departureTime || !flight.arrivalTime) {
        return "Duration unknown";
      }
      var departure = new Date(flight.departureTime);
      var arrival = new Date(flight.arrivalTime);
      if (Number.isNaN(departure.getTime()) || Number.isNaN(arrival.getTime())) {
        return "Duration unknown";
      }
      if (arrival < departure) {
        arrival.setDate(arrival.getDate() + 1);
      }
      var minutes = Math.max(0, Math.round((arrival - departure) / 60000));
      var hours = Math.floor(minutes / 60);
      var remainder = minutes % 60;
      if (!hours) {
        return remainder + "m";
      }
      return hours + "h " + String(remainder).padStart(2, "0") + "m";
    }

    function money(value) {
      return Number(value || 0).toLocaleString("en-IN", {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
      });
    }

    function badgeClass(status) {
      var value = String(status || "").toLowerCase();
      if (value.includes("success") || value.includes("succeed") || value.includes("confirmed") || value.includes("sent") || value.includes("scheduled")) {
        return "success";
      }
      if (value.includes("pending")) {
        return "pending";
      }
      if (value.includes("fail") || value.includes("cancel") || value.includes("declined") || value.includes("refunded")) {
        return "danger";
      }
      return "neutral";
    }

    function seatTone(flight) {
      var seats = Number(flight && flight.availableSeats || 0);
      if (seats <= 0) {
        return "soldout";
      }
      if (seats <= 10) {
        return "low";
      }
      if (seats >= 25) {
        return "success";
      }
      return "available";
    }

    function gatewayStatusText() {
      if (vm.gatewayOnline === true) {
        return "Gateway online";
      }
      if (vm.gatewayOnline === false) {
        return "Gateway offline";
      }
      return "Gateway checking";
    }

    function gatewayClass() {
      if (vm.gatewayOnline === true) {
        return "online";
      }
      if (vm.gatewayOnline === false) {
        return "offline";
      }
      return "checking";
    }

    function workflowClass(step) {
      if (step === 1) {
        return vm.currentBooking ? "complete" : "active";
      }
      if (step === 2) {
        if (vm.currentPayment && String(vm.currentPayment.status || "").toLowerCase().includes("succeed")) {
          return "complete";
        }
        return vm.currentBooking ? "active" : "locked";
      }
      if (step === 3) {
        if (vm.lastNotification) {
          return "complete";
        }
        return vm.currentPayment ? "active" : "locked";
      }
      return "locked";
    }

    function updateGatewayFromError(error) {
      if (!error) {
        return;
      }
      vm.gatewayOnline = !(error.status === -1 || error.status === 502);
    }

    function normalizeError(error) {
      var data = error && error.data ? error.data : {};
      var message = data.message || data.error || error.statusText || "Request failed";
      if (error.status === -1) {
        message = "Could not reach the frontend proxy or backend gateway.";
      }
      if (Array.isArray(data.fieldErrors) && data.fieldErrors.length) {
        message += " " + data.fieldErrors.map(function (item) {
          return (item.field || "field") + ": " + item.message;
        }).join(" ");
      }
      return message;
    }

    function showSuccess(title, message) {
      showToast("success", title, message);
    }

    function showError(title, message) {
      showToast("danger", title, message);
    }

    function showToast(type, title, message) {
      vm.toast = { type: type, title: title, message: message };
      $timeout(function () {
        if (vm.toast.message === message) {
          vm.toast = {};
        }
      }, 7000);
      renderLucide();
    }

    function dismissToast() {
      vm.toast = {};
    }

    function renderLucide() {
      $timeout(function () {
        if (window.lucide) {
          window.lucide.createIcons();
        }
      });
    }
  }
})();
