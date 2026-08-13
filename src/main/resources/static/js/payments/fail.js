async function savePaymentFailure() {
	params = new URLSearchParams(window.location.search);

	const orderId = params.get("orderId");

	const code = params.get("code") || "PAYMENT_FAILED";

	const message = params.get("message") || "결제에 실패했습니다.";
  
	const errorCodeElement = document.getElementById("error-code");

	const errorMessageElement = document.getElementById("error-message");

      if (errorCodeElement) {
          errorCodeElement.textContent = code;
      }

      if (errorMessageElement) {
          errorMessageElement.textContent = message;
      }

	if (!orderId) {
		return;
	}

	try {
		await fetch("/api/payments/fail", {
			method: "POST",

			headers: {
				"Content-Type": "application/json",
				...getAuthHeaders()
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

function getAuthHeaders() {

    const token =
        localStorage.getItem("token");

    if (!token) {
        throw new Error("로그인이 필요합니다.");
    }

    return {
        "Authorization":
            `Bearer ${token}`
    };
}