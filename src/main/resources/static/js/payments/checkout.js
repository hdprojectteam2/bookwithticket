async function main() {

    const paymentData =
        document.getElementById(
            "payment-data"
        );

    const paymentButton =
        document.getElementById(
            "payment-button"
        );

    if (!paymentData || !paymentButton) {
        console.error(
            "결제 화면 정보를 찾을 수 없습니다."
        );
        return;
    }


    const pageOrderNumber =
        paymentData.dataset.orderNumber;


    if (!pageOrderNumber) {
        alert(
            "주문번호를 확인할 수 없습니다."
        );
        return;
    }


    try {

        const response =
            await fetch(
                "/api/payments/checkout?orderNumber="
                + encodeURIComponent(
                    pageOrderNumber
                ),
                {
                    headers:
                        getAuthHeaders()
                }
            );


        if (!response.ok) {

            const error =
                await response.json();

            throw new Error(
                error.message
                || "결제 정보를 불러오지 못했습니다."
            );
        }


        const checkout =
            await response.json();


        const orderNumber =
            checkout.orderNumber;

        const orderName =
            checkout.orderName;

        const totalPrice =
            Number(
                checkout.totalPrice
            );

        const clientKey =
            checkout.clientKey;


        document.getElementById(
            "order-number"
        ).textContent =
            orderNumber;


        document.getElementById(
            "order-name"
        ).textContent =
            orderName;


        document.getElementById(
            "total-price"
        ).textContent =
            totalPrice.toLocaleString();


        const tossPayments =
            TossPayments(clientKey);


        const payment =
            tossPayments.payment({
                customerKey:
                    TossPayments.ANONYMOUS
            });


        paymentButton.disabled =
            false;


        paymentButton.addEventListener(
            "click",
            async function () {

                paymentButton.disabled =
                    true;

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
                            orderNumber,

                        orderName:
                            orderName,

                        successUrl:
                            window.location.origin
                            + "/payments/success",

                        failUrl:
                            window.location.origin
                            + "/payments/fail",

                        customerName:
                            "테스트 구매자"
                    });

                } catch (error) {

                    console.error(
                        "토스 결제창 오류:",
                        error
                    );

                    paymentButton.disabled =
                        false;
                }
            }
        );

    } catch (error) {

        console.error(
            "결제 정보 조회 오류:",
            error
        );

        alert(
            error.message
        );
    }
}


main();


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