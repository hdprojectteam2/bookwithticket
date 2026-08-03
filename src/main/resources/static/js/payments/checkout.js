async function cancelOrder(orderNumber) {
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
  const paymentData =
    document.getElementById("payment-data");

  const paymentButton =
    document.getElementById("payment-button");

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

  try {
    const clientKey = "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm";

    const tossPayments = TossPayments(clientKey);

    const widgets = tossPayments.widgets({
        customerKey: TossPayments.ANONYMOUS,
      });

    await widgets.setAmount({
      currency: "KRW",
      value: totalPrice,
    });

    await widgets.renderPaymentMethods({
      selector: "#payment-method",
      variantKey: "DEFAULT",
    });

    await widgets.renderAgreement({
      selector: "#agreement",
      variantKey: "AGREEMENT",
    });

    paymentButton.disabled = false;

    paymentButton.addEventListener(
      "click",
      async function () {
        paymentButton.disabled = true;

        try {
          await widgets.requestPayment({
            orderId: orderNumber,
            orderName: orderName,

            successUrl:
              window.location.origin + "/payments/success",

            failUrl:
              window.location.origin + "/payments/fail",
          });

        } catch (error) {
          console.error(
            "결제 요청 중단:",
            error
          );

          try {
            await cancelOrder(orderNumber);

            alert("결제가 취소되었습니다.");

            window.location.href = "/cart";

          } catch (cancelError) {
            console.error("주문 취소 처리 실패:", cancelError);

            alert("주문 취소 처리에 실패했습니다.");

            paymentButton.disabled = false;
          }
        }
      }
    );

  } catch (error) {
    console.error(error);

    alert(error.message || "결제위젯을 불러오지 못했습니다.");
  }
}

main();