const params = new URLSearchParams(location.search);

const type = params.get("type");

const id = params.get("id");

const deliveryStatus = params.get("deliveryStatus");

window.onload = function() {

    const token = localStorage.getItem("token");

    if (!token) {

        alert("로그인이 필요합니다.");

        window.close();

        return;
    }


    if (!type || !id) {

        alert("환불 정보를 확인할 수 없습니다.");

        window.close();

        return;
    }

    renderRefundPage();

    const refundButton = document.getElementById("refundButton");

    refundButton.addEventListener("click", submitRefund);
};



function renderRefundPage() {

    const reasonList = document.getElementById("reasonList");

    const returnSection = document.getElementById("returnSection");

    const description = document.getElementById("refundDescription");


    let reasons = [];


    if (type === "performance") {

        reasons = [
            "단순 변심",
            "일정 변경",
            "예매 실수",
            "기타"
        ];

        description.textContent = "환불 사유를 선택해주세요.";
        returnSection.hidden = true;
    }


    else if (type === "book") {

        reasons = [
            "단순 변심",
            "주문 실수",
            "상품 불량",
            "오배송",
            "기타"
        ];


        const needReturn = deliveryStatus === "배송 중" || deliveryStatus === "배송 완료";


        returnSection.hidden = !needReturn;


        if (needReturn) {
            description.textContent = "환불 사유와 반품 방법을 선택해주세요.";

        } else {

            description.textContent = "환불 사유를 선택해주세요.";
        }
    }


    else {

        alert("잘못된 환불 요청입니다.");

        window.close();

        return;
    }


    reasonList.innerHTML =
        reasons
            .map(
                reason => `
                    <label
                        class="refund-option"
                    >
                        <input
                            type="radio"
                            name="refundReason"
                            value="${reason}"
                        >

                        ${reason}

                    </label>
                `
            )
            .join("");


    document
        .querySelectorAll(
            'input[name="refundReason"]'
        )
        .forEach(
            radio => {

                radio.addEventListener(
                    "change",
                    handleReasonChange
                );
            }
        );
}



function handleReasonChange() {

    const selectedReason = document.querySelector('input[name="refundReason"]:checked');

    const etcReasonBox = document.getElementById("etcReasonBox");

    const etcReason = document.getElementById("etcReason");

    if (selectedReason && selectedReason.value === "기타") {
        etcReasonBox.hidden = false;
        etcReason.focus();

    } else {

        etcReasonBox.hidden = true;

        etcReason.value = "";
    }
}


async function submitRefund() {

    const selectedReason = document.querySelector('input[name="refundReason"]:checked');


    if (!selectedReason) {

        alert("환불 사유를 선택해주세요.");

        return;
    }

    let reason = selectedReason.value;


    if (reason === "기타") {

        const etcReason = document.getElementById("etcReason").value.trim();

        if (!etcReason) {
            alert("환불 사유를 입력해주세요.");
            return;
        }


        reason =
            "기타 - " + etcReason;
    }


    const needReturn = type === "book" && (deliveryStatus === "배송 중" || deliveryStatus === "배송 완료");


    let returnMethod = null;
    if (needReturn) {

        const selectedReturnMethod = document.querySelector('input[name="returnMethod"]:checked');

        if (!selectedReturnMethod) {

            alert("반품 방법을 선택해주세요.");

            return;
        }

        returnMethod = selectedReturnMethod.value;
    }



    if (!confirm(needReturn ? "환불을 신청하시겠습니까?" : "환불을 진행하시겠습니까?")) {
        return;
    }


    const refundButton = document.getElementById("refundButton");

    refundButton.disabled = true;

    try {
        const response =
            await fetch(
                `/api/payments/${encodeURIComponent(id)}/refund`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json",

                        ...getAuthHeaders()
                    },

                    body:
                        JSON.stringify({
                            reason: reason,
                            returnMethod: returnMethod
                        })
                }
            );


        let result = null;


        try {

            result =
                await response.json();

        } catch (error) {

            result = null;
        }


        if (!response.ok) {

            throw new Error(result?.message || "환불 요청 처리에 실패했습니다.");
        }


        alert(result?.message || (needReturn ? "환불 신청이 완료되었습니다." : "환불 처리가 완료되었습니다."));

        updateParentHistory();

        window.close();

    } catch (error) {
        console.error("환불 처리 오류:", error);

        alert(error.message);

        refundButton.disabled = false;
    }
}



function updateParentHistory() {

    if (!window.opener || window.opener.closed) {
        return;
    }

    if (type === "book") {
        if (typeof window.opener.loadBookHistory === "function") {
            window.opener.loadBookHistory();
        }

        if (typeof window.opener.loadMyBookHistory === "function") {
            window.opener.loadMyBookHistory();
        }

    }

    if (type === "performance") {
        if (typeof window.opener.loadPerformanceHistory === "function") {
            window.opener.loadPerformanceHistory();
        }

        if (typeof window.opener.loadMyPerformanceReservations === "function") {
            window.opener.loadMyPerformanceReservations();
        }

    }
}



function getAuthHeaders() {

    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("로그인이 필요합니다.");
    }


    return {
        "Authorization":
            `Bearer ${token}`
    };
}