window.onload = function() {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "/login.html";
        return;
    }
    loadBookHistory();
    loadPerformanceHistory();

    historyTab("bookHistory");
};

let bookHistoryVisibleCount = 3;
let performanceHistoryVisibleCount = 3;



function historyTab(tabId) {
    const bookHistory = document.getElementById("bookHistory");
    const ticketHistory = document.getElementById("ticketHistory");

    const bookSubTitle = document.getElementById("book_sub_title");
    const ticketSubTitle = document.getElementById("ticket_sub_title");

    const bookTab = document.getElementById("bookTab");
    const ticketTab = document.getElementById("ticketTab");

    const isBookTab = tabId === "bookHistory";

    bookHistory.hidden = !isBookTab;
    bookSubTitle.hidden = !isBookTab;

    ticketHistory.hidden = isBookTab;
    ticketSubTitle.hidden = isBookTab;

    bookTab.classList.toggle("active", isBookTab);
    ticketTab.classList.toggle("active", !isBookTab);
}


async function loadBookHistory() {
    const box = document.getElementById("bookHistory");

    try {
        const response =
            await fetch(
                "/api/history/books",
                {
                    headers: getAuthHeaders()
                }
            );

        if (!response.ok) {
            throw new Error("도서 구매내역 조회에 실패했습니다.");
        }

        const histories = await response.json();

        if (histories.length === 0) {
            box.innerHTML = `
                <div class="empty-history">
                    구매한 도서가 없습니다.
                </div>
            `;
            return;
        }

        const visibleHistories = histories.slice(0, bookHistoryVisibleCount);

        let html =
            visibleHistories
                .map(history => createBookHistory(history))
                .join("");

        if (histories.length > bookHistoryVisibleCount) {

            html += `
		        <button
		            type="button"
		            class="history-more-button"
		            onclick="showMoreBookHistory()"
		        >
		            더보기
		        </button>
		    `;
        }

        box.innerHTML = html;

    } catch (error) {
        console.error("도서 구매내역 조회 오류:", error);

        box.innerHTML = `
            <div class="empty-history">
                구매내역을 불러오지 못했습니다.
            </div>
        `;
    }
}


function createBookHistory(history) {
    const deliveryStatus = history.refundStatusCode !== "COMPLETED" ? getDeliveryBadge(history.deliveryStatus) : "";

    const refundStatus = getRefundStatus(history.refundStatus);

    const refundButton = getBookRefundButton(history);

    const orderItems = history.orderItems
        .map(item => createBookItem(item))
        .join("");

    return `
        <article class="history-card">

            <div class="history-top">
                <span class="orderNumber">
                    주문번호: ${history.orderNumber}
                </span>

				<div class="history-status">
					${deliveryStatus}
					${refundStatus}
				</div>
            </div>

            
			
			<div class="history-bottom">
	            <div class="history-item-list">
	                ${orderItems}
	            </div>
	
	            <div class="history-actions">
					<button
				        type="button"
				        class="detail-button"
				        onclick="moveToDetail('book', '${history.orderNumber}')">
				        상세보기
				    </button>
	                ${refundButton}
	            </div>
			</div>

        </article>
    `;
}


function createBookItem(item) {
    const quantityText = item.quantity > 1 ? ` × ${item.quantity}` : "";

    const price = item.quantity > 1 ? item.totalPrice : item.unitPrice;

    return `
        <div class="book-item">

            <img
                class="book-image"
                src="${item.imageUrl}"
                alt="${item.bookTitle}"
            >

            <div class="book-info">

                <h3 class="book-title">
                    ${item.bookTitle}${quantityText}
                </h3>

				<div class="book-detail-box">
                
					<span>
						${item.author || ""}
					</span>
	
					<span>·</span>
	
					<span>
						${item.publisher || ""}
					</span>
					
				</div>
					
                <p class="book-price">
                    ${formatPrice(price)}원
                </p>

            </div>

        </div>
    `;
}


async function loadPerformanceHistory() {
    const box = document.getElementById("ticketHistory");

    try {
        const response =
            await fetch(
                "/api/history/performances",
                {
                    headers: getAuthHeaders()
                }
            );

        if (!response.ok) {
            throw new Error("티켓 구매내역 조회에 실패했습니다.");
        }

        const histories = await response.json();

        if (histories.length === 0) {
            box.innerHTML = `
                <div class="empty-history">
                    구매한 티켓이 없습니다.
                </div>
            `;
            return;
        }

		const visibleHistories = histories.slice(0, performanceHistoryVisibleCount);

		let html =
		    visibleHistories
		        .map(history => createPerformanceHistory(history))
		        .join("");

		if (histories.length > performanceHistoryVisibleCount) {

		    html += `
		        <button
		            type="button"
		            class="history-more-button"
		            onclick="showMorePerformanceHistory()"
		        >
		            더보기
		        </button>
		    `;
		}

		box.innerHTML = html;

    } catch (error) {
        console.error("티켓 구매내역 조회 오류:", error);

        box.innerHTML = `
            <div class="empty-history">
                구매내역을 불러오지 못했습니다.
            </div>
        `;
    }
}

function showMoreBookHistory() {
    bookHistoryVisibleCount += 3;
    loadBookHistory();
}


function showMorePerformanceHistory() {
    performanceHistoryVisibleCount += 3;
    loadPerformanceHistory();
}


function createPerformanceHistory(history) {
    const reservationStatus = getReservationBadge(history.reservationStatus, history.reservationStatusCode);

    const refundStatus = getRefundStatus(history.refundStatus);

    const refundButton = getPerformanceRefundButton(history);

    return `
        <article class="history-card">

            <div class="history-top">

                <span class="orderNumber">
                    주문번호: ${history.reservationNumber}
                </span>

                <div class="history-status">
                    ${reservationStatus}
                    ${refundStatus}
                </div>

            </div>

            <div class="history-bottom">

                <div class="history-item-list">

                    <div class="performance-item">

                        <img
                            class="performance-image"
                            src="${history.posterUrl}"
                            alt="${history.performanceTitle}"
                        >

                        <div class="performance-info">

                            <h3 class="performance-title">
                                ${history.performanceTitle}
                            </h3>

                            <div class="performance-detail-box">
                                <p>${history.venue}</p>
                                <p>${formatDateTime(history.performanceStartAt)}</p>
                            </div>

                            <p class="performance-seat">
                                좌석: ${history.seatNumber}
                            </p>

                        </div>

                    </div>

                </div>

                <div class="history-actions">
					<button
				        type="button"
				        class="detail-button"
				        onclick="moveToDetail('performance', '${history.reservationNumber}')">
				        상세보기
				    </button>
				
                    ${refundButton}
                </div>

            </div>

        </article>
    `;
}



function getDeliveryBadge(status) {
    const statusClass = {
        "배송 준비 중": "preparing",
        "배송 중": "shipping",
        "배송 완료": "completed"
    }[status] || "";

    return `
        <span class="status-badge delivery-status ${statusClass}">
            ${status}
        </span>
    `;
}


function getReservationBadge(status, statusCode) {
    const statusClass = {
        "CONFIRMED": "confirmed",
        "CANCELLED": "cancelled"
    }[statusCode] || "";

    return `
        <span class="status-badge reservation-status ${statusClass}">
            ${status}
        </span>
    `;
}


function getRefundStatus(refundStatus) {
    if (!refundStatus) {
        return "";
    }

    const statusClass = {
        "환불 요청 중": "requested",
        "환불 완료": "completed",
        "환불 거절": "rejected"
    }[refundStatus] || "";

    return `
        <span class="status-badge refund-status ${statusClass}">
            ${refundStatus}
        </span>
    `;
}



function getBookRefundButton(history) {
    const canRefund =
        history.orderStatusCode === "PAID"
        && history.refundStatusCode == null;

    if (!canRefund) {
        return "";
    }

    return `
        <button
            type="button"
            class="refund-button"
            onclick="moveToRefundPage('book', '${history.orderNumber}', '${history.deliveryStatus}')"
        >
            환불 신청
        </button>
    `;
}


function getPerformanceRefundButton(history) {
    const canRefund = history.reservationStatusCode === "CONFIRMED" && history.refundStatusCode == null;

    if (!canRefund) {
        return "";
    }

    return `
        <button
            type="button"
            class="refund-button"
            onclick="moveToRefundPage('performance', '${history.reservationNumber}', '')"
        >
            환불 신청
        </button>
    `;
}

function moveToRefundPage(type, id, deliveryStatus) {
    const url = "/refund.html?type=" + encodeURIComponent(type) + "&id=" + encodeURIComponent(id) 	+ "&deliveryStatus=" + encodeURIComponent(deliveryStatus || "");;

    const width = 500;
    const height = 650;

    window.open(url, "_blank", `width=${width},height=${height}`);
}


function formatPrice(price) {
    return Number(price).toLocaleString("ko-KR");
}

function formatDateTime(dateTime) {
    if (!dateTime) {
        return "";
    }

    const date = new Date(dateTime);

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");

    return `${year}-${month}-${day} ${hour}:${minute}`;
}

function moveToDetail(type, id) {
    const url = "/history/detail?type=" + encodeURIComponent(type) + "&id=" + encodeURIComponent(id);

    const width = 600;
    const height = 500;

    window.open(url, "_blank", `width=${width},height=${height}`);
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