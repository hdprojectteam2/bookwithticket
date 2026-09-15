window.onload =
    async function() {
        const token = localStorage.getItem("token");

        if (!token) {
            alert("로그인이 필요합니다.");

            location.href = "/login.html";

            return;
        }

        const cartItemIds = getCartItemIds();

        if (cartItemIds.length === 0) {
            alert("주문할 상품 정보가 없습니다.");

            location.replace("/cart");

            return;
        }

        try {

            await loadOrderPreview(cartItemIds);


        } catch (error) {

            console.error("주문 정보 조회 오류:", error);

            alert(error.message);

            location.replace("/cart");
        }
    };


function getCartItemIds() {
    const data = sessionStorage.getItem("orderCartItemIds");


    if (!data) {
        return [];
    }


    try {
        const cartItemIds =
            JSON.parse(
                data
            );


        if (!Array.isArray(cartItemIds)) {
            return [];
        }

        return cartItemIds;


    } catch (error) {

        console.error(
            "장바구니 주문 정보 오류:",
            error
        );

        return [];
    }
}

async function loadOrderPreview(cartItemIds) {

    const response =
        await fetch(
            "/api/orders/preview",
            {
                method:
                    "POST",

                headers: {

                    "Content-Type":
                        "application/json",

                    ...getAuthHeaders()
                },

                body:
                    JSON.stringify({

                        cartItemIds:
                            cartItemIds
                    })
            }
        );


    if (!response.ok) {
        const message = await response.text();

        throw new Error(message || "주문 정보를 불러오지 못했습니다.");
    }

    const preview = await response.json();

    renderOrderPreview(preview);
}

function renderOrderPreview(preview) {
	const discountPrice = preview.originalPrice - preview.totalPrice;

    setText("summary-quantity", `${preview.totalQuantity}개`);
	
	setText("summary-original-price",`${formatPrice(preview.originalPrice)}원`);

	setText("summary-discount-price", `-${formatPrice(discountPrice)}원`);
	
    setText("summary-total-price", `${formatPrice(preview.totalPrice)}원`);

    setText("totalPrice", formatPrice(preview.totalPrice));
}


async function submitOrder() {
    const paymentButton = document.getElementById("payment-button");

    if (paymentButton && paymentButton.disabled) {
        return;
    }

    const cartItemIds = getCartItemIds();


    if (cartItemIds.length === 0) {
        alert("주문할 상품 정보가 없습니다.");

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

    if (!detailAddress) {
        alert("상세 주소를 입력해주세요.");
        return;
    }

    const requestData = {

        cartItemIds:
            cartItemIds,

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


    let createdOrderNumber = null;

    try {
        if (paymentButton) {
            paymentButton.disabled = true;
        }


        const response =
            await fetch(
                "/api/orders",
                {
                    method:
                        "POST",

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

            throw new Error(message || "주문 생성에 실패했습니다.");
        }


        const order = await response.json();

        createdOrderNumber = order.orderNumber;

        await requestTossPayment(createdOrderNumber);

    } catch (error) {

        console.error("주문 처리 오류:", error);

        if (createdOrderNumber) {

            try {

                await cancelBookOrder(
                    createdOrderNumber
                );


            } catch (cancelError) {

                console.error("주문 취소 실패:",cancelError);
            }
        }

        alert(error.message);

        if (paymentButton) {
            paymentButton.disabled = false;
        }
    }
}

async function requestTossPayment(orderNumber) {

    const member = await getCurrentMember();

    const response =
        await fetch(
            "/api/payments/checkout?orderNumber=" + encodeURIComponent(orderNumber),
            {
                headers:
                    getAuthHeaders()
            }
        );


    if (!response.ok) {
        let message = "결제 정보를 불러오지 못했습니다.";
        try {

            const error = await response.json();
            message = error.message || message;

        } catch (e) {

            const text = await response.text();
            if (text) {
                message = text;
            }
        }

        throw new Error(message);
    }

    const checkout = await response.json();
    const paymentOrderNumber = checkout.orderNumber;
    const orderName = String(checkout.orderName || "도서 주문").slice(0, 90);
    const totalPrice = Number(checkout.totalPrice);
    const clientKey = checkout.clientKey;

    if (!paymentOrderNumber || !orderName || !Number.isInteger(totalPrice) || totalPrice <= 0 || !clientKey) {
        throw new Error("결제 정보가 올바르지 않습니다.");
    }


    const tossPayments = TossPayments(clientKey);
	
    const payment = tossPayments.payment({ customerKey: TossPayments.ANONYMOUS });


    try {

        await payment.requestPayment({

            method:
                "CARD",


            amount: {

                currency:
                    "KRW",

                value:
                    totalPrice
            },

            orderId:
                paymentOrderNumber,

            orderName:
                orderName,

            successUrl:
                window.location.origin + "/payments/success",

            failUrl:
                window.location.origin + "/payments/fail",

            customerName:
                member.name
        });


    } catch (error) {

        console.error("토스 결제창 종료:", error);

        try {

            await cancelBookOrder(orderNumber);

            alert("결제가 취소되었습니다.");
			
			const paymentButton = document.getElementById("payment-button");

			if (paymentButton) {
				paymentButton.disabled = false;
			}
			
        } catch (cancelError) {

            console.error("주문 취소 처리 실패:", cancelError);

            alert("결제가 중단되었습니다.");
			
			const paymentButton = document.getElementById("payment-button");

			if (paymentButton) {
				paymentButton.disabled = false;
			}
        }
    }
}

async function getCurrentMember() {

    const response =
        await fetch(
            "/members/me",
            {
                method:
                    "GET",

                headers:
                    getAuthHeaders()
            }
        );


    if (!response.ok) {

        throw new Error(
            "회원 정보를 불러오지 못했습니다."
        );
    }


    return await response.json();
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

document.addEventListener("DOMContentLoaded", () => {

	const memberInfoButton = document.getElementById("member-info-button");

	if (memberInfoButton) {
		memberInfoButton.addEventListener("click", fillMemberInfo);
	}
});

async function fillMemberInfo() {

	try {
		const response = await fetch("/api/orders/member-info", {
			headers: getAuthHeaders()
		});

		if (!response.ok) {
			const message = await response.text();

			throw new Error(message || "회원 정보를 불러오지 못했습니다.");
		}

		const member = await response.json();

		document.getElementById("recipient").value = member.name ?? "";

		document.getElementById("phone").value = member.phone ?? "";

		document.getElementById("zipcode").value = member.zipcode ?? "";

		document.getElementById("address").value = member.address ?? "";

		document.getElementById("detailAddress").value = member.detailAddress ?? "";

	} catch (error) {
		alert(error.message);
	}
}

function back() {
	history.back()
}