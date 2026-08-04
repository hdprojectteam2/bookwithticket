async function confirmPayment() {
  const params = new URLSearchParams(window.location.search);

  const paymentKey = params.get("paymentKey");

  const orderId = params.get("orderId");

  const amount = Number(params.get("amount"));

  const titleElement = document.getElementById("payment-title");

  const messageElement = document.getElementById("payment-message");

  if (!paymentKey || !orderId || !Number.isInteger(amount) || amount <= 0) {
   
	 titleElement.textContent = "결제 정보 오류";

    messageElement.textContent = "결제 정보가 올바르지 않습니다.";

    return;
  }

  try {
    const response =
      await fetch("/api/payments", {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify({
          paymentKey: paymentKey,
          orderId: orderId,
          amount: amount,
        }),
      });

    const result = await response.json();

    if (!response.ok) {
      const code = encodeURIComponent(result.code || "PAYMENT_CONFIRM_FAILED");

      const message = encodeURIComponent(result.message || "결제 승인에 실패했습니다.");

      window.location.href = `/payments/fail?code=${code}&message=${message}`;

      return;
    }

    titleElement.textContent = "결제를 완료했어요";

    messageElement.textContent = "주문 결제가 정상적으로 완료되었습니다.";

    document.getElementById("orderId").textContent = result.orderNumber;

    document.getElementById("amount").textContent = result.amount.toLocaleString() + "원";

  } catch (error) {
    console.error(error);

    titleElement.textContent = "결제 처리 오류";

    messageElement.textContent = "결제 승인 처리 중 오류가 발생했습니다.";
  }
}

confirmPayment();