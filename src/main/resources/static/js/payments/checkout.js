async function cancelBookOrder(orderNumber) {
  const response = await fetch(
    `/api/orders/${orderNumber}/cancel`,
    {
      method: "POST",
    }
  );

  if (!response.ok) {
    const message = await response.text();

    throw new Error(
      message || "주문 취소 처리에 실패했습니다."
    );
  }
}

async function main() {
  const paymentData = document.getElementById("payment-data");

  const paymentButton = document.getElementById("payment-button");

  if (!paymentData || !paymentButton) {
    console.error("결제 화면 정보를 찾을 수 없습니다.");
    return;
  }

  const orderNumber = paymentData.dataset.orderNumber;

  const orderName = paymentData.dataset.orderName;

  const totalPrice = Number(paymentData.dataset.totalPrice);

  if (!orderNumber) {
    alert("주문번호를 확인할 수 없습니다.");
    return;
  }

  if (!orderName) {
    alert("주문 상품 정보를 확인할 수 없습니다.");
    return;
  }

  if (
    !Number.isInteger(totalPrice) || totalPrice <= 0
  ) {
    alert("결제 금액이 올바르지 않습니다.");
    return;
  }


  const clientKey =  paymentData.dataset.clientKey;
  
  if (!clientKey) {
    alert("토스 클라이언트 키를 확인할 수 없습니다.");
    return;
  }	

  const tossPayments = TossPayments(clientKey);


  const payment =
    tossPayments.payment({
      customerKey:
        TossPayments.ANONYMOUS,
    });

  paymentButton.disabled = false;

  paymentButton.addEventListener(
    "click",
    async function () {
      paymentButton.disabled = true;

	  try {
	    await payment.requestPayment({
	      method: "CARD",

	      amount: {
	        currency: "KRW",
	        value: totalPrice,
	      },

	      orderId: orderNumber,
	      orderName: orderName,

	      successUrl: window.location.origin + "/payments/success",

	      failUrl: window.location.origin + "/payments/fail",

	      customerName: "테스트 구매자",
	    });

	  } catch (error) {
	    console.error("토스 결제창 오류:", error);
	    console.error("오류 코드:", error.code);
	    console.error("오류 메시지:", error.message);


	    paymentButton.disabled = false;
	  }
    }
  );
}

main();