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

        await loadPerformanceDetail(id);

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

    const content = document.getElementById("detail-content");

    if (!content) {
        console.error("detail-content 요소를 찾을 수 없습니다.");

        return;
    }


    content.innerHTML = `

        <section class="detail-section">
			
			<span class="bk-eyebrow">Order History</span>
            <div class="title">
                주문 내역
            </div>
			
			<div class="detail-item-list">
			
	            <div class="detail-row">
	
	                <span>
	                    주문번호 : 
	                </span>
	
	                <strong>
	                    ${order.orderNumber}
	                </strong>
	
	            </div>
	
	
	            <div class="detail-row">
	
	                <span>
	                    주문일시 : 
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
	                    배송 상태 : 
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
				                    택배사 : 
				                </span>
	
				                <strong>
				                    ${order.courier || "-"}
				                </strong>
	
				            </div>
	
	
				            <div class="detail-row">
	
				                <span>
				                    송장번호 : 
				                </span>
	
				                <strong>
				                    ${order.trackingNumber}
				                </strong>
	
				            </div>
							
							<button
								type="button"
							    class="tracking-button"
							    onclick="openDeliveryTracking(
							    	'${order.courier}',
							        '${order.trackingNumber}'
							 	)"
							>
							 	택배 조회
							</button>
				        `
	
				        : ``
				}					
	
	            ${refundStatus}
				
			</div>

        </section>

        <section class="detail-section">
		
			<span class="bk-eyebrow">Ordered product</span>

		
            <div class="title">
                주문 상품
            </div>


            <div class="detail-item-list">

                ${orderItems}

            </div>

        </section>

        <section class="detail-section">

			<span class="bk-eyebrow">Recipient information</span>
		
            <div class="title">
                수신자 정보
            </div>


			<div class="receiver-box">

			    <div class="detail-row">
			        <span>받는 사람 : </span>

			        <strong>
			            ${order.receiverName || "-"}
			        </strong>
			    </div>

			    <div class="detail-row">
			        <span>연락처 : </span>

			        <strong>
			            ${order.receiverPhone || "-"}
			        </strong>
			    </div>

			    <div class="detail-row">
			        <span>배송지 : </span>

			        <strong>
			            ${order.address || "-"}
			        </strong>
			    </div>

			</div>
			
        </section>

        <section class="detail-section">

			<span class="bk-eyebrow">Payment information</span>
		
            <div class="title">
                결제 정보
            </div>


            <div class="payment-box">


                <div class="payment-total-row">

                    <span>
                        주문금액 : 
                    </span>

                    <strong>
                        총 ${formatPrice(order.totalPrice)}원
                    </strong>

                </div>


                <div class="detail-row">

                    <span>
                        결제수단 : 
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
                        결제금액 : 
                    </span>

                    <strong>
                        ${formatPrice(order.totalPrice)}원
                    </strong>

                </div>


                <div class="detail-row">

                    <span>
                        결제일시 : 
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

    `;
}



function createBookDetailItem(item) {
	
	const imageUrl = item.imageUrl || "";

    return `

        <div class="detail-book-item">

			<div class="detail-image-wrap">

				${
					imageUrl
						? `
							<img
								class="detail-image"
								src="${imageUrl}"
								alt="${item.bookTitle || "도서 이미지"}"
								onerror="
									this.style.display='none';
									this.nextElementSibling.style.display='flex';
								"
							>

							<div
								class="detail-image-placeholder"
								style="display:none;"
							>
								B
							</div>
						`
						: `
							<div class="detail-image-placeholder">
								B
							</div>
						`
				}

			</div>


			<div class="detail-book-info">

			    <h3>
			        ${item.bookTitle || "-"}
			    </h3>

			    <div class="book-meta">
			        ${item.author || ""}
			        ${
			            item.author && item.publisher
			                ? " · "
			                : ""
			        }
			        ${item.publisher || ""}
			    </div>

			    <div class="book-info-row">
			        <span>수량 : </span>

			        <strong>
			            ${item.quantity}
			        </strong>
			    </div>

			    <div class="book-info-row">
			        <span>상품 금액 : </span>

			        <strong>
			            ${formatPrice(item.totalPrice)}원
			        </strong>
			    </div>

			</div>

        </div>
    `;
}


async function loadPerformanceDetail(reservationNumber) {

    try {

        const response =
            await fetch(
                `/api/history/performances/${encodeURIComponent(reservationNumber)}`,
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

	const posterUrl = reservation.posterUrl || "";

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



    const content = document.getElementById("detail-content");


    if (!content) {
        console.error("detail-content 요소를 찾을 수 없습니다.");
        return;
    }


    content.innerHTML = `

        <section class="detail-section">

			<span class="bk-eyebrow">Reservation information</span>

            <div class="title">
                예매 정보
            </div>


            <div class="detail-row">

                <span>
                    예매번호 : 
                </span>

                <strong>
                    ${reservation.reservationNumber}
                </strong>

            </div>


            <div class="detail-row">

                <span>
                    예매 상태 : 
                </span>

                <strong>
                    ${reservation.reservationStatus}
                </strong>

            </div>


            ${refundStatus}

        </section>

        <section class="detail-section">
	
			<span class="bk-eyebrow">Performance information</span>

            <div class="title">
                공연 정보
            </div>


            <div class="detail-performance">


				<div class="detail-image-wrap">
	
				    ${
				        posterUrl
				            ? `
				                <img
				                    class="detail-image"
				                    src="${posterUrl}"
				                    alt="${reservation.performanceTitle || "공연 이미지"}"
				                    onerror="
				                        this.style.display='none';
				                        this.nextElementSibling.style.display='flex';
				                    "
				                >
	
				                <div
				                    class="detail-image-placeholder performance"
				                    style="display:none;"
				                >
				                    🎭
				                </div>
				            `
				            : `
				                <div class="detail-image-placeholder performance">
				                    🎭
				                </div>
				            `
				    }
	
				</div>


				<div class="detail-performance-info">

				    <h3>
				        ${reservation.performanceTitle || "-"}
				    </h3>

				    <div class="detail-row">
				        <span>공연장</span>

				        <strong>
				            ${reservation.venue || "-"}
				        </strong>
				    </div>

				    <div class="detail-row">
				        <span>공연 일시</span>

				        <strong>
				            ${
				                reservation.performanceStartAt
				                    ? formatDateTime(
				                        reservation.performanceStartAt
				                    )
				                    : "-"
				            }
				        </strong>
				    </div>

				    <div class="detail-row">
				        <span>좌석</span>

				        <strong>
				            ${reservation.seatNumber || "-"}
				        </strong>
				    </div>

				</div>

            </div>

        </section>

        <section class="detail-section">
	
			<span class="bk-eyebrow">Payment information</span>

            <div class="title">
                결제 정보
            </div>


            <div class="payment-box">


                <div class="payment-total-row">

                    <span>
                        결제금액 : 
                    </span>

                    <strong>
                        ${formatPrice(
                            reservation.totalPrice
                        )}원
                    </strong>

                </div>


                <div class="detail-row">

                    <span>
                        결제수단 : 
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
                        결제일시 : 
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

    `;
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

function openDeliveryTracking(courier, trackingNumber) {

    if (!courier) {
        alert("택배사 정보가 없습니다.");
        return;
    }


    if (!trackingNumber) {
        alert("등록된 송장번호가 없습니다.");
        return;
    }

    const url ="/delivery/tracking"+ `?courier=${encodeURIComponent(courier)}&invoice=${encodeURIComponent(trackingNumber)}`;

	location.href = url;
}
