async function submitOrder() {
    const orderNumber = new URLSearchParams(window.location.search)
        .get("orderNumber");

    if (!orderNumber) {
        alert("주문 번호를 찾을 수 없습니다.");
        return;
    }

    const requestData = {
        recipient: document.getElementById("recipient").value,
        phone: document.getElementById("phone").value,
        zipcode: document.getElementById("zipcode").value,
        address: document.getElementById("address").value,
        detailAddress: document.getElementById("detailAddress").value,
        deliveryRequest: document.getElementById("deliveryRequest").value
    };

    const response = await
    fetch(`/api/orders/${orderNumber}/delivery`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestData)
    });

    if (!response.ok) {
        const message = await
        response.text();
        alert(message || "배송지 저장에 실패했습니다.");
        return;
    }

    location.href = `/payments/checkout?orderNumber=${orderNumber}`;
}