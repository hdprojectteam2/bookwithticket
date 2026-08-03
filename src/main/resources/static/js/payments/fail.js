async function savePaymentFailure() {
  const params =
    new URLSearchParams(window.location.search);

  const orderId =
    params.get("orderId");

  const code = params.get("code") || "PAYMENT_FAILED";

  const message = params.get("message") || "결제에 실패했습니다.";

  if (!orderId) {
    return;
  }

  try {
    await fetch("/api/payments/fail", {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        orderId: orderId,
        code: code,
        message: message,
      }),
    });

  } catch (error) {
    console.error( "결제 실패 기록 저장 실패:", error);
  }
}

savePaymentFailure();