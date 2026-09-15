async function savePaymentFailure() {

    const params =
        new URLSearchParams(window.location.search);

    const orderId =
        params.get("orderId");

    const code =
        params.get("code")
        || "PAYMENT_FAILED";

    const message =
        params.get("message")
        || "결제에 실패했습니다.";


    setText(
        "error-code",
        code
    );

    setText(
        "error-message",
        message
    );


    if (!orderId) {
        return;
    }


    try {

        const response =
            await fetch(
                "/api/payments/fail",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json",

                        ...getAuthHeaders()
                    },

                    body:
                        JSON.stringify({
                            orderId:
                                orderId,

                            code:
                                code,

                            message:
                                message
                        })
                }
            );


        if (!response.ok) {

            const responseMessage =
                await response.text();

            console.error(
                "결제 실패 기록 저장 실패:",
                responseMessage
            );
        }

    } catch (error) {

        console.error(
            "결제 실패 기록 저장 중 오류:",
            error
        );
    }
}



function setupRetryButton() {

    const params = new URLSearchParams(window.location.search);

    const orderId = params.get("orderId");

    const retryButton = document.getElementById("retry-button");


    if (!retryButton) {
        return;
    }


    const isPerformance = orderId && orderId.startsWith("PERF_");


    if (isPerformance) {

        retryButton.textContent = "다시 결제하기";


        retryButton.addEventListener(
            "click",
            function() {
                const reservationId = orderId.replace(/^PERF_/, "");

                location.href = `/payments/checkout?orderNumber=${encodeURIComponent(reservationId)}`;
            }
        );

    } else {

        retryButton.textContent =
            "장바구니로 돌아가기";


        retryButton.addEventListener(
            "click",
            function() {

                location.href =
                    "/cart";
            }
        );
    }
}



function setupHomeButton() {

    const homeButton =
        document.getElementById(
            "home-button"
        );


    if (!homeButton) {
        return;
    }


    homeButton.addEventListener(
        "click",
        function() {

            location.href =
                "/mainpage.html";
        }
    );
}



function getAuthHeaders() {

    const token =
        localStorage.getItem(
            "token"
        );


    if (!token) {
        throw new Error(
            "로그인이 필요합니다."
        );
    }


    return {
        "Authorization":
            `Bearer ${token}`
    };
}



function setText(
    elementId,
    value
) {

    const element =
        document.getElementById(
            elementId
        );


    if (!element) {
        return;
    }


    element.textContent =
        value ?? "";
}



savePaymentFailure();

setupRetryButton();

setupHomeButton();