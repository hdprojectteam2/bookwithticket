window.onload = function () {
    loadDetail();
};


async function loadDetail() {

    const params = new URLSearchParams(window.location.search);

    const type = params.get("type");

    const id = params.get("id");


    if (!type || !id) {
        showError("잘못된 상세 페이지 요청입니다.");
        return;
    }


    if (type === "book") {
        await loadBookDetail(id);
        return;
    }


    if (type === "performance") {

        const reservationId = id.replace(/^PERF_/, "");

        await loadPerformanceDetail(reservationId);

        return;
    }


    showError("지원하지 않는 구매내역 유형입니다.");
}


async function loadBookDetail(orderNumber) {

    try {

        const response =
            await fetch(
                `/api/history/books/${encodeURIComponent(orderNumber)}`,
				{
					headers: getAuthHeaders()
				}
            );

        if (!response.ok) {
            throw new Error("주문 상세 정보를 불러오지 못했습니다.");
        }

        const order = await response.json();

        renderBookDetail(order);


    } catch (error) {
        console.error("도서 주문 상세 조회 오류:", error);

        showError(error.message);
    }
}


function renderBookDetail(order) {

    setDetailTitle("도서 주문 상세");

    const orderItems =
        order.orderItems
            .map(item =>
                createBookDetailItem(item)
            )
            .join("");


    const refundStatus =
        order.refundStatus

            ? `
                <div class="detail-row">

                    <span>
                        환불 상태
                    </span>

                    <strong>
                        ${order.refundStatus}
                    </strong>

                </div>
            `

            : "";


    const refundButton = order.orderStatusCode === "PAID" && order.refundStatusCode == null

            ? `
                <button
                    type="button"
                    class="refund-button"
                    onclick="requestRefund('${order.orderNumber}')"
                >
                    환불 신청
                </button>
            `

            : "";


    const content = document.getElementById("detail-content");


    if (!content) {

        console.error("detail-content 요소를 찾을 수 없습니다.");

        return;
    }


    content.innerHTML = `

        <section class="detail-section">

            <h2>
                주문 정보
            </h2>

            <div class="detail-row">

                <span>
                    주문번호
                </span>

                <strong>
                    ${order.orderNumber}
                </strong>

            </div>


            <div class="detail-row">

                <span>
                    주문일시
                </span>

                <strong>
                    ${
                        order.orderedAt
                            ? formatDateTime(
                                order.orderedAt
                            )
                            : "-"
                    }
                </strong>

            </div>


            <div class="detail-row">

                <span>
                    배송 상태
                </span>

                <strong>
                    ${order.deliveryStatus || "-"}
                </strong>

            </div>
			
			${
			    order.trackingNumber

			        ? `
			            <div class="detail-row">

			                <span>
			                    택배사
			                </span>

			                <strong>
			                    ${order.courier || "-"}
			                </strong>

			            </div>


			            <div class="detail-row">

			                <span>
			                    송장번호
			                </span>

			                <strong>
			                    ${order.trackingNumber}
			                </strong>

			            </div>
			        `

			        : ``
			}					

            ${refundStatus}

        </section>

        <section class="detail-section">

            <h2>
                주문상품
            </h2>


            <div class="detail-item-list">

                ${orderItems}

            </div>

        </section>

        <section class="detail-section">

            <h2>
                수신자정보
            </h2>


            <div class="receiver-box">

                <strong class="receiver-name">

                    ${order.receiverName || "-"}

                </strong>


                <p>
                    ${order.receiverPhone || "-"}
                </p>


                <p>
                    ${order.address || "-"}
                </p>

            </div>

        </section>

        <section class="detail-section">

            <h2>
                결제정보
            </h2>


            <div class="payment-box">


                <div class="payment-total-row">

                    <span>
                        주문금액
                    </span>

                    <strong>
                        총 ${formatPrice(order.totalPrice)}원
                    </strong>

                </div>


                <div class="detail-row">

                    <span>
                        결제수단
                    </span>

                    <strong>
                        ${
                            getPaymentMethodName(
                                order.paymentMethod
                            )
                        }
                    </strong>

                </div>


                <div class="detail-row">

                    <span>
                        결제금액
                    </span>

                    <strong>
                        ${formatPrice(order.totalPrice)}원
                    </strong>

                </div>


                <div class="detail-row">

                    <span>
                        결제일시
                    </span>

                    <strong>
                        ${
                            order.paidAt
                                ? formatDateTime(
                                    order.paidAt
                                )
                                : "-"
                        }
                    </strong>

                </div>

            </div>

        </section>

        <div class="detail-actions">

            ${refundButton}


            <button
                type="button"
                onclick="location.href='/history'"
            >
                목록
            </button>

        </div>
    `;
}



function createBookDetailItem(item) {

    return `

        <div class="detail-book-item">

            <img
                class="detail-image"
                src="${item.imageUrl || ""}"
                alt="${item.bookTitle || "도서 이미지"}"
            >


            <div>

                <h3>
                    ${item.bookTitle || "-"}
                </h3>


                <p>

                    ${item.author || ""}

                    ${
                        item.author
                        && item.publisher

                            ? " · "

                            : ""
                    }

                    ${item.publisher || ""}

                </p>


                <p>
                    수량:
                    ${item.quantity}
                </p>


                <p>
                    상품 금액:
                    ${formatPrice(
                        item.totalPrice
                    )}원
                </p>

            </div>

        </div>
    `;
}


async function loadPerformanceDetail(reservationId) {

    try {

        const response =
            await fetch(
                `/api/history/performances/${encodeURIComponent(reservationId)}`,
				{
					headers: getAuthHeaders()
				}
            );


        if (!response.ok) {

            throw new Error("예매 상세 정보를 불러오지 못했습니다.");
        }


        const reservation = await response.json();


        renderPerformanceDetail(reservation);


    } catch (error) {

        console.error("공연 예매 상세 조회 오류:", error);


        showError(error.message);
    }
}



function renderPerformanceDetail(reservation) {

    setDetailTitle("공연 예매 상세");


    const refundStatus = reservation.refundStatus

            ? `
                <div class="detail-row">

                    <span>
                        환불 상태
                    </span>

                    <strong>
                        ${reservation.refundStatus}
                    </strong>

                </div>
            `

            : "";


    const refundButton = reservation.reservationStatusCode === "CONFIRMED" && reservation.refundStatusCode == null

            ? `
                <button
                    type="button"
                    class="refund-button"
                    onclick="requestRefund('${reservation.reservationNumber}')"
                >
                    환불 신청
                </button>
            `

            : "";


    const content = document.getElementById("detail-content");


    if (!content) {
        console.error("detail-content 요소를 찾을 수 없습니다.");
        return;
    }


    content.innerHTML = `

        <section class="detail-section">

            <h2>
                예매 정보
            </h2>


            <div class="detail-row">

                <span>
                    예매번호
                </span>

                <strong>
                    ${reservation.reservationNumber}
                </strong>

            </div>


            <div class="detail-row">

                <span>
                    예매 상태
                </span>

                <strong>
                    ${reservation.reservationStatus}
                </strong>

            </div>


            ${refundStatus}

        </section>

        <section class="detail-section">

            <h2>
                공연 정보
            </h2>


            <div class="detail-performance">


                <img
                    class="detail-image"
                    src="${reservation.posterUrl || ""}"
                    alt="${reservation.performanceTitle || "공연 이미지"}"
                >


                <div>

                    <h3>
                        ${reservation.performanceTitle || "-"}
                    </h3>


                    <p>

                        공연장:

                        ${reservation.venue || "-"}

                    </p>


                    <p>

                        공연 일시:

                        ${
                            reservation.performanceStartAt

                                ? formatDateTime(
                                    reservation.performanceStartAt
                                )

                                : "-"
                        }

                    </p>


                    <p>

                        좌석:

                        ${reservation.seatNumber || "-"}

                    </p>

                </div>

            </div>

        </section>

        <section class="detail-section">

            <h2>
                결제정보
            </h2>


            <div class="payment-box">


                <div class="payment-total-row">

                    <span>
                        결제금액
                    </span>

                    <strong>
                        ${formatPrice(
                            reservation.totalPrice
                        )}원
                    </strong>

                </div>


                <div class="detail-row">

                    <span>
                        결제수단
                    </span>

                    <strong>
                        ${
                            getPaymentMethodName(
                                reservation.paymentMethod
                            )
                        }
                    </strong>

                </div>


                <div class="detail-row">

                    <span>
                        결제일시
                    </span>

                    <strong>

                        ${
                            reservation.paidAt

                                ? formatDateTime(
                                    reservation.paidAt
                                )

                                : "-"
                        }

                    </strong>

                </div>

            </div>

        </section>

        <div class="detail-actions">

            ${refundButton}


            <button
                type="button"
                onclick="location.href='/history'"
            >
                목록
            </button>

        </div>
    `;
}


async function requestRefund(id) {

    const reason = prompt("환불 사유를 입력해 주세요.");


    if (reason === null) {
        return;
    }


    const trimmedReason =reason.trim();


    if (!trimmedReason) {
        alert("환불 사유를 입력해 주세요.");
        return;
    }


    if (!confirm("환불하시겠습니까?")) {
        return;
    }


    try {
        const response =
            await fetch(

                `/api/payments/${encodeURIComponent(id)}/refund`,

                {
                    method: "POST",

                    headers: {
                        "Content-Type":"application/json",
						...getAuthHeaders()
                    },

                    body: JSON.stringify({
                        reason: trimmedReason
                    })
                }
            );


        let result = null;


        try {
            result = await response.json();
        } catch (error) {
            result = null;
        }


        if (!response.ok) {

            throw new Error(

                result?.message || "환불 요청 처리에 실패했습니다."
            );
        }


        alert(
            result?.message || "환불 요청이 처리되었습니다."
        );


        await loadDetail();


    } catch (error) {

        console.error("환불 처리 오류:", error);


        alert(error.message);
    }
}


function setDetailTitle(title) {

    const titleElement = document.getElementById("detail-title");


    if (titleElement) {
        titleElement.textContent = title;
    }
}



function formatPrice(price) {

    if (price === null || price === undefined) {
        return "0";
    }


    return Number(price).toLocaleString("ko-KR");
}



function showError(message) {
    const box = document.getElementById("detail-content");
    if (!box) {
        console.error("detail-content 요소를 찾을 수 없습니다.", message);
        return;
    }


    box.innerHTML = `

        <div class="empty-detail">

            ${message}

        </div>
    `;
}



function formatDateTime(dateTime) {
    if (!dateTime) {
        return "";
    }


    const date = new Date(dateTime);


    if (Number.isNaN(date.getTime())) {
        return "";
    }


    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day =String(date.getDate()).padStart(2, "0");
    const hour = String(date.getHours()).padStart(2, "0");
    const minute =String(date.getMinutes()).padStart(2, "0");

    return `${year}-${month}-${day} ${hour}:${minute}`;
}



function getPaymentMethodName(method) {

    const paymentMethods = {

        CARD:
            "카드",

        ACCOUNT_TRANSFER:
            "계좌이체",

        TRANSFER:
            "계좌이체",

        VIRTUAL_ACCOUNT:
            "가상계좌",

        MOBILE_PHONE:
            "휴대폰",

        EASY_PAY:
            "간편결제",

        TOSS_PAY:
            "토스페이",

        PAYCO:
            "페이코",

        KAKAO_PAY:
            "카카오페이",

        NAVER_PAY:
            "네이버페이",

        SSG_PAY:
            "SSG페이",

        L_PAY:
            "엘페이",

        SAMSUNG_PAY:
            "삼성페이",

        UNKNOWN:
            "기타"
    };


    return paymentMethods[method] || method || "-";
}

function getAuthHeaders() {

    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("로그인이 필요합니다.");
    }

    return {
        "Authorization":
            `Bearer ${token}`
    };
}