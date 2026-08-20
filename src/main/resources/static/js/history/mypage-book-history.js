document.addEventListener("DOMContentLoaded", function() {
    loadMyBookHistory();
});

//구매 내역에서 보여줄 기본 도서 개수
let bookHistoryVisibleCount = 3;

async function loadMyBookHistory() {

    const box = document.getElementById("myBookHistory");

    if (!box) {
        return;
    }

    const token = localStorage.getItem("token") || localStorage.getItem("accessToken");

    if (!token || token === "undefined") {

        box.innerHTML = `
            <div class="empty-history">
                로그인이 필요합니다.
            </div>
        `;

        return;
    }


    try {

        const response = await fetch(
            "/api/history/books",
            {
                headers: {
                    "Authorization": `Bearer ${token}`
                }
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
		        .map(history => createMyBookHistory(history))
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

function showMoreBookHistory() {

    bookHistoryVisibleCount += 3;

    loadMyBookHistory();
}



function createMyBookHistory(history) {
    const deliveryStatus = history.refundStatusCode !== "COMPLETED" ? getMyDeliveryBadge(history.deliveryStatus) : "";

    const refundStatus = getMyRefundStatus(history.refundStatus);

    const refundButton = getMyBookRefundButton(history);

    const orderItems = history.orderItems.map(item => createMyBookItem(item)).join("");

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
                        onclick="moveToBookDetail('${history.orderNumber}')"
                    >
                        상세보기
                    </button>

                    ${refundButton}

                </div>

            </div>

        </article>
    `;
}



function createMyBookItem(item) {
    const quantityText = item.quantity > 1 ? ` × ${item.quantity}` : "";

    const price = item.quantity > 1 ? item.totalPrice : item.unitPrice;


    return `
        <div class="book-item">

            <img
                class="book-image"
                src="${item.imageUrl || ""}"
                alt="${item.bookTitle || "도서 이미지"}"
            >


            <div class="book-info">

                <h3 class="book-title">
                    ${item.bookTitle || "-"}${quantityText}
                </h3>


                <div class="book-detail-box">

                    <span>
                        ${item.author || ""}
                    </span>

                    ${item.author && item.publisher
            ? "<span>·</span>"
            : ""
        }

                    <span>
                        ${item.publisher || ""}
                    </span>

                </div>


                <p class="book-price">
                    ${formatMyPrice(price)}원
                </p>

            </div>

        </div>
    `;
}



function getMyDeliveryBadge(status) {

    const statusClass = {

        "배송 준비 중": "preparing",

        "배송 중": "shipping",

        "배송 완료": "completed"

    }[status] || "";


    return `
        <span class="status-badge delivery-status ${statusClass}">
            ${status || ""}
        </span>
    `;
}



function getMyRefundStatus(refundStatus) {

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


function getMyBookRefundButton(history) {

    const canRefund = history.orderStatusCode === "PAID" && history.refundStatusCode == null;


    if (!canRefund) {
        return "";
    }


    return `
        <button
            type="button"
            class="refund-button"
            onclick="requestMyBookRefund('${history.orderNumber}')"
        >
            환불 신청
        </button>
    `;
}


async function requestMyBookRefund(orderNumber) {
    const reason = prompt("환불 사유를 입력해 주세요.");

    if (reason === null) {
        return;
    }


    const trimmedReason = reason.trim();

    if (!trimmedReason) {
        alert("환불 사유를 입력해 주세요.");
        return;
    }


    if (!confirm("해당 주문을 환불하시겠습니까?")) {
        return;
    }

    const token = localStorage.getItem("token") || localStorage.getItem("accessToken");

    try {

        const response = await fetch(
            `/api/payments/${encodeURIComponent(orderNumber)}/refund`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
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
            throw new Error(result?.message || "환불 요청 처리에 실패했습니다.");
        }

        alert(result?.message || "환불 요청이 처리되었습니다.");

        await loadMyBookHistory();


    } catch (error) {
        console.error("환불 처리 오류:", error);
        alert(error.message);
    }
}



function moveToBookDetail(orderNumber) {
    const url = "/history/detail?type=book&id=" + encodeURIComponent(orderNumber);
	
	const width = 900;
	const height = 800;
	
	window.open(url, "_blank", `width=${width},height=${height}`);
}


function formatMyPrice(price) {
    return Number(price || 0).toLocaleString("ko-KR");
}