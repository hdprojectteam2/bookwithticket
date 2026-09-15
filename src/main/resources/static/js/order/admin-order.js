let openedOrderNumber = null;
let openedReservationId = null;

window.addEventListener(
    "DOMContentLoaded",
    initializeAdminPage
);


async function initializeAdminPage() {

    try {

        const isAdmin =
            await checkAdmin();

        if (!isAdmin) {
            return;
        }

        await loadOrders();

    } catch (error) {

        console.error(
            "관리자 페이지 접근 오류:",
            error
        );
    }
}

async function checkAdmin() {

    const token = localStorage.getItem("token");

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "/login.html";
        return false;
    }


    const response =
        await fetch(
            "/api/admin/check",
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );


    if (response.ok) {
        return true;
    }


    if (response.status === 403) {
        alert("관리자만 접근할 수 있습니다.");
        location.href = "/";
        return false;
    }


    if (response.status === 401) {
        alert("로그인이 필요합니다.");
        location.href = "/login.html";

        return false;
    }


    alert("관리자 페이지에 접근할 수 없습니다.");

    location.href = "/";

    return false;
}

async function loadOrders() {

    try {

        const response =
            await fetch(
                "/api/admin/orders",
                {
                    headers:
                        getAuthHeaders()
                }
            );


        if (!response.ok) {

            throw new Error(
                await getErrorMessage(
                    response
                )
            );
        }


        const orders = await response.json();

        renderOrders(orders);

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}

function renderOrders(orders) {

    const orderList = document.getElementById("order-list");

    if (!Array.isArray(orders) || orders.length === 0) {

        orderList.innerHTML = `
            <tr>
                <td colspan="7">
                    판매 내역이 없습니다.
                </td>
            </tr>
        `;

        return;
    }


    orderList.innerHTML =
        orders
            .map(order => {

                return `

                    <tr>

                        <td>
                            ${escapeHtml(order.orderNumber)}
                        </td>


                        <td>
                            ${formatDate(order.orderedAt)}
                        </td>


                        <td>
                            ${escapeHtml(order.memberId ?? "-")}
                        </td>


                        <td>
                            ${formatPrice(order.totalPrice)}원
                        </td>


                        <td>
                            ${getDeliveryStatusText(order.deliveryStatus)}
                        </td>


                        <td>
                            ${getRefundStatusText(order.refundStatus)}
                        </td>


                        <td>

                            <button
                                type="button"
                                onclick="toggleDetail(
                                    '${order.orderNumber}'
                                )">

                                상세/관리

                            </button>

                        </td>

                    </tr>


                    <tr
                        class="detail-row"
                        id="detail-${order.orderNumber}">

                        <td colspan="7">

                            ${createOrderDetail(
                    order
                )}

                        </td>

                    </tr>

                `;
            })
            .join("");


    if (openedOrderNumber) {

        const openedRow =
            document.getElementById(`detail-${openedOrderNumber}`);

        if (openedRow) {
            openedRow.style.display = "table-row";

        } else {

            openedOrderNumber = null;
        }
    }
}

function createOrderDetail(order) {

    return `

        <div class="detail-box">


            <div class="detail-section">

                <h3>
                    주문 정보
                </h3>

                <div>
                    회원번호:
                    ${order.memberId ?? "-"}
                </div>

                <div>
                    주문번호:
                    ${escapeHtml(order.orderNumber)}
                </div>

                <div>
                    주문일:
                    ${formatDate(order.orderedAt)}
                </div>

                <div>
                    주문상태:
                    ${escapeHtml(order.orderStatus ?? "-")}
                </div>

            </div>



            <div class="detail-section">

                <h3>
                    배송지 정보
                </h3>

                <div>
                    수령인:
                    ${escapeHtml(order.receiverName ?? "-")}
                </div>

                <div>
                    연락처:
                    ${escapeHtml(order.phone ?? "-")}
                </div>

                <div>
                    우편번호:
                    ${escapeHtml(order.zipCode ?? "-")}
                </div>

                <div>
                    주소:
                    ${escapeHtml(order.address ?? "-")}
                </div>

                <div>
                    상세주소:
                    ${escapeHtml(order.detailAddress ?? "-")}
                </div>

                <div>
                    배송요청사항:
                    ${escapeHtml(order.deliveryRequest ?? "-")}
                </div>

            </div>



            <div class="detail-section">

                <h3>
                    구매 상품
                </h3>

                ${createOrderItems(order.items)}

            </div>



            <div class="detail-section">

                <h3>
                    택배 정보
                </h3>


                <select
                    id="courier-${order.orderNumber}">

                    ${createCourierOptions(order.courier)}

                </select>


                <input
                    type="text"
                    id="tracking-${order.orderNumber}"
                    value="${escapeAttribute(order.trackingNumber ?? "")}"
                    placeholder="송장번호"
                >


                <button
                    type="button"
                    onclick="saveTracking(
                        '${order.orderNumber}'
                    )">

                    송장정보 저장

                </button>

            </div>



            <div class="detail-section">

                <h3>
                    배송 상태
                </h3>


                <select
                    id="delivery-status-${order.orderNumber}">

                    ${createDeliveryStatusOptions(order.deliveryStatus)}

                </select>


                <button
                    type="button"
                    onclick="saveDeliveryStatus(
                        '${order.orderNumber}'
                    )">

                    배송상태 변경

                </button>

            </div>



            <div class="detail-section">

                <h3>
                    환불
                </h3>

                ${createRefundControl(
        order
    )}

            </div>


        </div>
    `;
}

function createOrderItems(items) {

    if (
        !Array.isArray(items)
        || items.length === 0
    ) {

        return `
            <div>
                주문 상품이 없습니다.
            </div>
        `;
    }


    return items
        .map(item => `

            <div class="order-item">

                <strong>
                    ${escapeHtml(
            item.bookTitle
        )}
                </strong>

                <br>

                ${formatPrice(
            item.price
        )}원

                ×

                ${item.quantity}개

                =

                ${formatPrice(
            item.totalPrice
        )}원

            </div>

        `)

        .join("");
}

function toggleDetail(orderNumber) {

    const selectedRow = document.getElementById(`detail-${orderNumber}`);


    if (!selectedRow) {
        return;
    }

    if (openedOrderNumber === orderNumber && selectedRow.style.display === "table-row") {

        selectedRow.style.display = "none";

        openedOrderNumber = null;

        return;
    }

    if (openedOrderNumber) {
        const previousRow = document.getElementById(`detail-${openedOrderNumber}`);
        if (previousRow) {
            previousRow.style.display = "none";
        }
    }

    selectedRow.style.display = "table-row";

    openedOrderNumber = orderNumber;
}

async function saveTracking(orderNumber) {

    const courier =
        document.getElementById(
            `courier-${orderNumber}`
        ).value;


    const trackingNumber =
        document.getElementById(
            `tracking-${orderNumber}`
        )
            .value
            .trim();

    if (!courier) {
        alert("택배사를 선택해주세요.");
        return;
    }


    if (!trackingNumber) {
        alert("송장번호를 입력해주세요.");
        return;
    }


    const response =
        await fetch(
            `/api/admin/orders/${encodeURIComponent(orderNumber)}/tracking`,
            {
                method:
                    "PUT",

                headers: {

                    "Content-Type":
                        "application/json",

                    ...getAuthHeaders()
                },

                body:
                    JSON.stringify({

                        courier:
                            courier,

                        trackingNumber:
                            trackingNumber
                    })
            }
        );


    if (!response.ok) {

        alert(
            await getErrorMessage(
                response
            )
        );

        return;
    }


    alert("송장정보가 저장되었습니다.");


    await loadOrders();
}

async function saveDeliveryStatus(orderNumber) {

    const deliveryStatus =
        document.getElementById(
            `delivery-status-${orderNumber}`
        ).value;


    const response =
        await fetch(
            `/api/admin/orders/${encodeURIComponent(orderNumber)}/delivery-status`,
            {
                method:
                    "PUT",

                headers: {

                    "Content-Type":
                        "application/json",

                    ...getAuthHeaders()
                },

                body:
                    JSON.stringify({

                        deliveryStatus:
                            deliveryStatus
                    })
            }
        );


    if (!response.ok) {

        alert(
            await getErrorMessage(
                response
            )
        );

        return;
    }


    alert("배송상태가 변경되었습니다.");


    await loadOrders();
}

function createRefundControl(order) {
	
	const returnMethodText = getReturnMethodText(order.returnMethod);

    if (order.refundStatus === "REQUESTED" && order.refundId) {

        return `

            <div class="refund-reason">

                환불 사유:

                ${escapeHtml(
            order.refundReason
            ?? "-"
        )}

            </div>
			
			<div class="refund-return-method">

				반품 방법:
			    ${escapeHtml(
					returnMethodText
				)}
				
			</div>

            <button
                type="button"
                onclick="approveRefund(
                    ${order.refundId}
                )">

                환불 승인

            </button>


            <button
                type="button"
                onclick="rejectRefund(
                    ${order.refundId}
                )">

                환불 거절

            </button>
        `;
    }

    if (
        order.refundStatus === "REJECTED"
        && order.refundId
    ) {

        return `

	            <div class="refund-reason">

	                환불 사유:

	                ${escapeHtml(
            order.refundReason
            ?? "-"
        )}

	            </div>
				
				<div class="refund-return-method">

					반품 방법:
				    ${escapeHtml(
						returnMethodText
					)}
					
				</div>


	            <div>
	                환불 거절
	            </div>


	            <button
	                type="button"
	                onclick="approveRefund(
	                    ${order.refundId}
	                )">

	                환불 승인

	            </button>
	        `;
    }

    if (order.refundStatus === "COMPLETED") {
        return `

		        <div class="refund-reason">

			        환불 사유:
	
			        ${escapeHtml(
            order.refundReason
            ?? "-"
        )}

		        </div>
				
				<div class="refund-return-method">

					반품 방법:
				    ${escapeHtml(
						returnMethodText
					)}
					
				</div>


		        <div>
		        	환불 완료
		        </div>
		        `;
    }

    if (order.orderStatus === "PAID") {

        return `
	        <div>환불 요청 없음</div>

	        <button
	            type="button"
	            onclick="forceRefund('${order.orderNumber}')">
	            환불
	        </button>
	    `;
    }

    return "환불 불가";
}

function getReturnMethodText(returnMethod) {

    if (!returnMethod) {
        return "-";
    }
	
    if (returnMethod === "PICKUP") {
        return "택배 수거 요청";
    }

    if (returnMethod === "SELF_SHIP") {
        return "직접 발송";
    }

    return returnMethod;
}

async function approveRefund(refundId) {

    if (!confirm("환불을 승인하시겠습니까?")) {
        return;
    }


    const response =
        await fetch(
            `/api/admin/refunds/${refundId}/approve`,
            {
                method:
                    "POST",

                headers:
                    getAuthHeaders()
            }
        );


    if (!response.ok) {

        alert(
            await getErrorMessage(
                response
            )
        );

        return;
    }


    alert("환불이 승인되었습니다.");


    await loadOrders();
}

async function forceRefund(orderNumber) {

    if (!confirm(
        "사용자의 환불 요청이 없는 주문입니다. 환불하시겠습니까?"
    )) {
        return;
    }


    const response =
        await fetch(
            `/api/admin/orders/${encodeURIComponent(orderNumber)}/force-refund`,
            {
                method: "POST",
                headers: getAuthHeaders()
            }
        );

    if (!response.ok) {
        alert(await getErrorMessage(response));
        return;
    }

    alert("환불이 완료되었습니다.");


    await loadOrders();
}


async function rejectRefund(refundId) {

    if (!confirm("환불을 거절하시겠습니까?")) {
        return;
    }


    const response =
        await fetch(
            `/api/admin/refunds/${refundId}/reject`,
            {
                method:
                    "POST",

                headers:
                    getAuthHeaders()
            }
        );



    if (!response.ok) {

        alert(
            await getErrorMessage(
                response
            )
        );

        return;
    }


    alert("환불이 거절되었습니다.");


    await loadOrders();
}

function createCourierOptions(currentCourier) {

    const couriers = [
        "",
        "우체국택배",
		"CJ대한통운",
        "한진택배",
        "로젠택배",
        "롯데택배",
        "일양로지스",
        "한의사랑택배",
        "천일택배",
        "건영택배",
        "한진택배B2B",
        "대신택배",
        "경동택배",
        "GS Postbox 택배",
        "합동택배",
        "애니트랙",
        "SLX택배",
        "우리택배(구호남택배)",
        "CU편의점택배",
        "농협택배",
		"홈픽택배",
		"IK물류",
		"성훈물류",
		"CR로지텍",
		"용마로지스",
		"원더스퀵",
		"컬리넥스트마일",
		"풀앳홈",
		"두발히어로",
		"위니아딤채",
		"지니고 당일배송",
		"카카오 T 당일배송"
    ];


    return couriers
        .map(courier => {

            const selected =
                courier === currentCourier
                    ? "selected"
                    : "";


            const text =
                courier
                    ? courier
                    : "택배사 선택";


            return `
                <option
                    value="${escapeAttribute(courier)}"
                    ${selected}>

                    ${escapeHtml(text)}

                </option>
            `;
        })

        .join("");
}


function createDeliveryStatusOptions(currentStatus) {

    const statuses = [

        {
            value:
                "READY",

            text:
                "배송 준비"
        },

        {
            value:
                "SHIPPING",

            text:
                "배송 중"
        },

        {
            value:
                "DELIVERED",

            text:
                "배송 완료"
        }
    ];


    return statuses
        .map(status => {

            const selected =
                status.value
                    === currentStatus

                    ? "selected"
                    : "";


            return `
                <option
                    value="${status.value}"
                    ${selected}>

                    ${status.text}

                </option>
            `;
        })

        .join("");
}

function getDeliveryStatusText(status) {

    if (status === "READY") {
        return "배송 준비";
    }

    if (status === "SHIPPING") {
        return "배송 중";
    }

    if (status === "DELIVERED") {
        return "배송 완료";
    }

    return status ?? "-";
}


function getRefundStatusText(status) {

    if (!status) {
        return "-";
    }

    if (status === "REQUESTED") {
        return "환불 요청";
    }

    if (status === "COMPLETED") {
        return "환불 완료";
    }

    if (status === "REJECTED") {
        return "환불 거절";
    }

    return status;
}

function getReservationStatusText(status) {

    if (status === "HELD") {
        return "좌석 선점 중";
    }

    if (status === "CONFIRMED") {
        return "예매 완료";
    }

    if (status === "CANCELLED") {
        return "예매 취소";
    }

    if (status === "EXPIRED") {
        return "선점 만료";
    }

    return status ?? "-";
}


function formatPrice(price) {

    return Number(
        price ?? 0
    ).toLocaleString(
        "ko-KR"
    );
}


function formatDate(value) {
    if (!value) {
        return "-";
    }


    const date = new Date(value);


    if (Number.isNaN(date.getTime())) {
        return value;
    }


    return date.toLocaleString("ko-KR");
}

function getAuthHeaders() {

    const token = localStorage.getItem("token");


    if (!token) {
        throw new Error("로그인이 필요합니다.");
    }


    return {

        Authorization:
            `Bearer ${token}`
    };
}


async function getErrorMessage(response) {

    const text = await response.text();


    if (!text) {
        return "요청 처리 중 오류가 발생했습니다.";
    }


    try {

        const data = JSON.parse(text);
        return data.message || data.error || text;

    } catch (error) {
        return text;
    }
}

function escapeHtml(value) {

    if (value === null || value === undefined) {
        return "";
    }


    return String(value)

        .replaceAll(
            "&",
            "&amp;"
        )

        .replaceAll(
            "<",
            "&lt;"
        )

        .replaceAll(
            ">",
            "&gt;"
        )

        .replaceAll(
            '"',
            "&quot;"
        )

        .replaceAll(
            "'",
            "&#039;"
        );
}


function escapeAttribute(value) {
    return escapeHtml(value);
}

function changeAdminTab(type) {
    const bookSection = document.getElementById("book-order-section");
    const performanceSection = document.getElementById("performance-order-section");
    const bookTab = document.getElementById("book-tab");
    const performanceTab = document.getElementById("performance-tab");


    if (type === "book") {
        bookSection.style.display = "block";
        performanceSection.style.display = "none";
        bookTab.classList.add("active");
        performanceTab.classList.remove("active");
        return;
    }


    if (type === "performance") {
        bookSection.style.display = "none";
        performanceSection.style.display = "block";
        bookTab.classList.remove("active");
        performanceTab.classList.add("active");
        loadPerformanceOrders();
    }
}

async function loadPerformanceOrders() {
    const response =
        await fetch(
            "/api/admin/reservations",
            {
                headers:
                    getAuthHeaders()
            }
        );


    if (!response.ok) {
        alert(await getErrorMessage(response));

        return;
    }


    const reservations = await response.json();

    renderPerformanceOrders(reservations);
	}

function renderPerformanceOrders(reservations) {

    const list =
        document.getElementById(
            "performance-order-list"
        );

    if (
        !Array.isArray(reservations)
        || reservations.length === 0
    ) {

        list.innerHTML = `
            <tr>
                <td colspan="9">
                    공연 예매 내역이 없습니다.
                </td>
            </tr>
        `;

        return;
    }


    list.innerHTML =
        reservations
            .map(reservation => `

                <tr>

                    <td>
                        ${reservation.reservationId}
                    </td>

                    <td>
                        ${formatDate(
                            reservation.reservedAt
                        )}
                    </td>

                    <td>
                        ${reservation.memberId}
                    </td>

                    <td>
                        ${escapeHtml(
                            reservation.performanceTitle
                        )}
                    </td>

                    <td>
                        ${formatDate(
                            reservation.performanceTime
                        )}
                    </td>

                    <td>
                        ${escapeHtml(
                            reservation.seatNumber
                        )}
                    </td>

                    <td>
                        ${formatPrice(
                            reservation.totalPrice
                        )}원
                    </td>

                    <td>
                        ${getReservationStatusText(
                            reservation.reservationStatus
                        )}
                    </td>

                    <td>
                        <button
                            type="button"
                            onclick="
                                togglePerformanceDetail(
                                    ${reservation.reservationId}
                                )
                            "
                        >
                            상세/관리
                        </button>
                    </td>

                </tr>


                <tr
                    id="performance-detail-${reservation.reservationId}"
                    style="display:none;"
                >
                    <td colspan="9">

                        ${createPerformanceDetail(
                            reservation
                        )}

                    </td>
                </tr>

            `)
            .join("");
}

function createPerformanceDetail(reservation) {

    return `
        <div class="detail-box">

            <div class="detail-section">

                <h3>
                    예매 정보
                </h3>

                <div>
                    예매번호:
                    ${reservation.reservationId ?? "-"}
                </div>

                <div>
                    회원번호:
                    ${reservation.memberId ?? "-"}
                </div>

                <div>
                    예매일:
					${reservation.reservedAt
						? formatDate(reservation.reservedAt)
					    : "-"
					}
                </div>

                <div>
                    예매상태:
                    ${getReservationStatusText(
                        reservation.reservationStatus
                    )}
                </div>

                <div>
                    좌석:
                    ${escapeHtml(
                        reservation.seatNumber ?? "-"
                    )}
                </div>

                <div>
                    결제금액:
                    ${formatPrice(
                        reservation.totalPrice
                    )}원
                </div>

            </div>


            <div class="detail-section">

                <h3>
                    환불
                </h3>

                ${createPerformanceRefundControl(
                    reservation
                )}

            </div>

        </div>
    `;
}

function createPerformanceRefundControl(reservation) {

    if (reservation.reservationStatus === "CONFIRMED") {

        return `
            <div class="refund-control">

                <div>
                    환불 요청 없음
                </div>

                <button
                    type="button"
                    onclick="
                        forcePerformanceRefund(
                            ${reservation.reservationId}
                        )
                    "
                >
                    환불
                </button>

            </div>
        `;
    }


    if (reservation.reservationStatus === "CANCELLED") {

        return `
            <div class="refund-control">
                환불 완료
            </div>
        `;
    }


    return `
        <div class="refund-control">
            환불 불가
        </div>
    `;
}

async function forcePerformanceRefund(reservationId) {

    if (!confirm("사용자의 환불 요청 없이 해당 공연 예매를 환불하시겠습니까?")) {
        return;
    }


    const response =
        await fetch(
            `/api/admin/reservations/${reservationId}/force-refund`,
            {
                method: "POST",
                headers: getAuthHeaders()
            }
        );


    if (!response.ok) {

        alert(await getErrorMessage(response));

        return;
    }


    alert("공연 환불이 완료되었습니다.");


    await loadPerformanceOrders();
}

function togglePerformanceDetail(reservationId) {

    const selectedRow = document.getElementById(`performance-detail-${reservationId}`);

    if (!selectedRow) {
        return;
    }

    if (openedReservationId === reservationId && selectedRow.style.display === "table-row") {
        selectedRow.style.display = "none";

        openedReservationId = null;

        return;
    }


    if (openedReservationId) {

        const previousRow =document.getElementById(`performance-detail-${openedReservationId}`);

        if (previousRow) {
            previousRow.style.display = "none";
        }
    }


    selectedRow.style.display = "table-row";

    openedReservationId = reservationId;
}
