async function confirmPayment() {

    const params = new URLSearchParams(window.location.search);

    const paymentKey = params.get("paymentKey");


    const orderId = params.get("orderId");


    const amount = Number(params.get("amount"));

    const isPerformance = orderId?.startsWith("PERF_");

    setPaymentTypeUI(isPerformance);

    if (!paymentKey || !orderId || !Number.isInteger(amount) || amount <= 0) {

        showError("결제 정보 오류", "결제 정보가 올바르지 않습니다.");
        return;
    }

    try {

        const response =
            await fetch(
                "/api/payments",
                {
                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json",

                        ...getAuthHeaders()
                    },

                    body:
                        JSON.stringify({

                            paymentKey:
                                paymentKey,

                            orderId:
                                orderId,

                            amount:
                                amount
                        })
                }
            );


        const result = await response.json();


        if (!response.ok) {
            const code = encodeURIComponent(result.code || "PAYMENT_CONFIRM_FAILED");
            const message = encodeURIComponent(result.message || "결제 승인에 실패했습니다.");
            const encodedOrderId = encodeURIComponent(orderId);

            location.href = `/payments/fail?code=${code}&message=${message}&orderId=${encodedOrderId}`;

            return;
        }

        if (isPerformance) {
            await showPerformanceSuccess(result, orderId, amount);

        } else {

            await showBookSuccess(result, orderId, amount);
        }


    } catch (error) {

        console.error("결제 처리 오류:", error);


        showError("결제 처리 오류", "결제 승인 처리 중 오류가 발생했습니다.");
    }
}

async function showBookSuccess(paymentResult, originalOrderId, requestAmount) {

    const orderNumber = paymentResult.orderNumber || paymentResult.orderId || originalOrderId;

    setText("payment-title", "주문이 완료되었습니다!");

    setText("number-label", "주문 번호:");

    setText("orderId", orderNumber);

    try {

        const order = await loadBookOrder(orderNumber);

        console.log("도서 주문 조회 결과:", order);

        console.log("도서 주문 상품:", order.orderItems);

        const orderName = getBookOrderName(order);

        setText("product-name", orderName);


    } catch (error) {

        console.error("도서 주문 정보 조회 실패:", error);

        setText("product-name", getBookOrderName(paymentResult)
        );
    }

    const paymentAmount = paymentResult.amount ?? requestAmount ?? 0;

    setText("amount", `${formatPrice(paymentAmount)}원`);

    setText("payment-message", "도서 주문 결제가 정상적으로 완료되었습니다.");
}



async function loadBookOrder(orderNumber) {
    const response =
        await fetch(
            `/api/orders/${encodeURIComponent(orderNumber)}/completed`,
            {
                headers:
                    getAuthHeaders()
            }
        );


    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "주문 정보를 불러오지 못했습니다.");
    }


    return await response.json();
}

function getBookOrderName(order) {

    if (!order) {
        return "-";
    }


    const items =
        Array.isArray(order.orderItems)
            ? order.orderItems

            : Array.isArray(order.items)
                ? order.items

                : [];


    if (items.length === 0) {
        return (order.bookTitle || order.orderName || order.productName || "-");
    }


    const firstItem = items[0];


    const firstBookTitle = firstItem.bookTitle || firstItem.title || firstItem.bookName || firstItem.productName || "도서";

    const otherCount = items.length - 1;

    if (otherCount > 0) {
        return (`${firstBookTitle}` + ` 외 ${otherCount}권`
        );
    }

    return firstBookTitle;
}



async function showPerformanceSuccess(paymentResult, originalOrderId, requestAmount) {

    const reservationNumber = paymentResult.reservationNumber || paymentResult.orderNumber || paymentResult.orderId || originalOrderId;


    setText("payment-title", "예매가 완료되었습니다!");


    setText("number-label", "예매 번호:");


    setText("orderId", reservationNumber);


    try {
        const reservation = await loadPerformanceReservation(reservationNumber);

        console.log("공연 예매 조회 결과:", reservation);

        renderPerformanceInfo(reservation);

    } catch (error) {
        console.error("공연 예매 정보 조회 실패:", error);

        renderPerformanceInfo(paymentResult);
    }


    const paymentAmount = paymentResult.amount ?? requestAmount ?? 0;


    setText("amount", `${formatPrice(paymentAmount)}원`);


    setText("payment-message", "공연 예매가 정상적으로 완료되었습니다.");
}

async function loadPerformanceReservation(reservationNumber) {

    const reservationId = parseReservationId(reservationNumber);

    const response =
        await fetch(
            `/api/history/performances/${encodeURIComponent(reservationId)}`,
            {
                headers:
                    getAuthHeaders()
            }
        );


    if (!response.ok) {
        const message = await response.text();

        throw new Error(message || "예매 정보를 불러오지 못했습니다.");
    }

    return await response.json();
}

function parseReservationId(reservationNumber) {

    if (!reservationNumber) {
        throw new Error("예매번호를 확인할 수 없습니다.");
    }


    let value = String(reservationNumber);


    if (value.startsWith("PERF_")) {
        value = value.substring("PERF_".length);
    }


    const reservationId = Number(value);

    if (!Number.isInteger(reservationId) || reservationId <= 0) {

        throw new Error("올바르지 않은 예매번호입니다.");
    }

    return reservationId;
}

function renderPerformanceInfo(reservation) {

    if (!reservation) {
        setText("performance-name", "-");

        setText("performance-venue", "-");

        setText("performance-seat", "-");

        return;
    }


    const performanceName = reservation.performanceTitle || reservation.performanceName || reservation.title || reservation.performance?.title || "-";


    const venue = reservation.venue || reservation.performanceVenue || reservation.venueName || reservation.performance?.venue || "-";

    const seatText = getSeatText(reservation);

    setText("performance-name", performanceName);

    setText("performance-venue", venue);

    setText("performance-seat", seatText);
}

function getSeatText(reservation) {

    if (Array.isArray(reservation.seats) && reservation.seats.length > 0) {

        return reservation.seats
            .map(seat => {

                if (typeof seat === "string") {
                    return seat;
                }

                return (seat.seatNumber || seat.number || seat.name || "");
            })
            .filter(Boolean)
            .join(", ");
    }

    if (Array.isArray(reservation.seatNumbers) && reservation.seatNumbers.length > 0) {
        return reservation.seatNumbers.join(", ");
    }


    if (reservation.seatNumber) {
        return reservation.seatNumber;
    }


    if (reservation.seat) {

        return (reservation.seat.seatNumber || reservation.seat.number || "-");
    }

    return "-";
}


function setPaymentTypeUI(isPerformance) {

    const bookSummary = document.getElementById("book-summary");

    const performanceSummary = document.getElementById("performance-summary");

    if (isPerformance) {
        setText("number-label", "예매 번호:");

        setText("history-button", "예매 내역 확인");

        if (bookSummary) {
            bookSummary.style.display = "none";
        }

        if (performanceSummary) {
            performanceSummary.style.display = "block";
        }

    } else {

        setText("number-label", "주문 번호:");

        setText("history-button", "구매 내역 확인");


        if (bookSummary) {
            bookSummary.style.display = "block";
        }


        if (performanceSummary) {
            performanceSummary.style.display = "none";
        }
    }

    const historyButton = document.getElementById("history-button");


    if (historyButton) {
        historyButton.onclick = function() {
            location.href = "/history";
        };
    }
}

function showError(title, message) {

    const card = document.querySelector(".success-card");


    if (card) {
        card.classList.add("error");
    }


    setText("payment-title", title);


    setText("payment-message", message);
}

function getAuthHeaders() {

    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("로그인이 필요합니다.");
    }


    return {
        "Authorization": `Bearer ${token}`
    };
}

function setText(elementId, value) {
    const element = document.getElementById(elementId);


    if (!element) {
        return;
    }

    element.textContent = value ?? "";
}

function formatPrice(price) {

    const number = Number(price ?? 0);


    if (!Number.isFinite(number)) {
        return "0";
    }


    return number.toLocaleString(
        "ko-KR"
    );
}


confirmPayment();