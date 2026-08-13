window.onload = async function() {
    const token = localStorage.getItem("token");

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "/login.html";
        return;
    }


    try {
        await loadOrder();

    } catch (error) {
        console.error("주문 정보 조회 오류:", error);

        alert(error.message);
		
		location.replace("/cart");
		return;
    }
};



async function loadOrder() {
    const orderNumber = getOrderNumber();

    if (!orderNumber) {
        throw new Error("주문 번호를 찾을 수 없습니다.");
    }


    const response =
        await fetch(
            `/api/orders/${encodeURIComponent(orderNumber)}`,
            {
                headers:
                    getAuthHeaders()
            }
        );


    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "주문 정보를 불러오지 못했습니다.");
    }


    const order = await response.json();


    renderOrder(order);
}

function renderOrder(order) {

    const items = Array.isArray(order.orderItems) ? order.orderItems : [];

    const totalQuantity =
        items.reduce(
            (sum, item) => {

                return sum
                    + Number(
                        item.quantity ?? 0
                    );
            },
            0
        );

    setText("summary-quantity", `${totalQuantity}개`);
    setText("summary-product-price", `${formatPrice(order.totalPrice)}원`);
    setText("summary-total-price", `${formatPrice(order.totalPrice)}원`);
    setText("totalPrice", formatPrice(order.totalPrice));

}



async function submitOrder() {
    const orderNumber = getOrderNumber();

    if (!orderNumber) {
        alert("주문 번호를 찾을 수 없습니다.");
        return;
    }


    const recipient = document.getElementById("recipient").value.trim();

    const phone = document.getElementById("phone").value.trim();

    const zipcode = document.getElementById("zipcode").value.trim();

    const address = document.getElementById("address").value.trim();

    const detailAddress = document.getElementById("detailAddress").value.trim();

    const deliveryRequest = document.getElementById("deliveryRequest").value.trim();

    if (!recipient) {
        alert("받는 분을 입력해주세요.");
        return;
    }

    if (!phone) {
        alert("연락처를 입력해주세요.");
        return;
    }

    if (!zipcode) {
        alert("우편번호를 입력해주세요.");
        return;
    }

    if (!address) {
        alert("주소를 입력해주세요.");
        return;
    }


    const requestData = {
        recipient:
            recipient,

        phone:
            phone,

        zipcode:
            zipcode,

        address:
            address,

        detailAddress:
            detailAddress,

        deliveryRequest:
            deliveryRequest
    };


    try {

        const response =
            await fetch(

                `/api/orders/${encodeURIComponent(orderNumber)}/delivery`,

                {
                    method:
                        "PUT",

                    headers: {

                        "Content-Type":
                            "application/json",

                        ...getAuthHeaders()
                    },

                    body:
                        JSON.stringify(
                            requestData
                        )
                }
            );

        if (!response.ok) {

            const message = await response.text();


            throw new Error(message || "배송지 저장에 실패했습니다.");
        }

        location.href = "/payments/checkout?orderNumber=" + encodeURIComponent(orderNumber);

    } catch (error) {
        console.error("배송지 저장 오류:", error);

        alert(error.message);
    }
}



async function cancelBookOrder(orderNumber) {

    if (!orderNumber) {
        throw new Error(
            "주문번호를 확인할 수 없습니다."
        );
    }


    const response =
        await fetch(

            `/api/orders/${encodeURIComponent(orderNumber)}/cancel`,

            {
                method:
                    "POST",

                headers:
                    getAuthHeaders()
            }
        );


    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "주문 취소 처리에 실패했습니다.");
    }
}



function getOrderNumber() {

    const params = new URLSearchParams(window.location.search);

    return params.get("orderNumber");
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



function setText(elementId, value) {
    const element = document.getElementById(elementId);
    if (!element) {
        return;
    }
    element.textContent = value ?? "";
}



function formatPrice(price) {
    if (price === null || price === undefined) {
        return "0";
    }
    return Number(price).toLocaleString("ko-KR");
}

function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}