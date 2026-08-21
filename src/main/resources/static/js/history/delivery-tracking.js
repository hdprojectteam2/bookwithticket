window.addEventListener("DOMContentLoaded",loadTrackingInfo);

async function loadTrackingInfo() {

    const params =new URLSearchParams(window.location.search);

    const courier = params.get("courier");

    const invoice = params.get("invoice");


    if (!courier || !invoice) {
        showTrackingError(
            "배송조회 정보가 올바르지 않습니다."
        );

        return;
    }


    try {

        const response =
            await fetch(
                `/api/delivery/tracking`
                + `?courier=${encodeURIComponent(courier)}`
                + `&invoice=${encodeURIComponent(invoice)}`,

                {
                    headers:
                        getAuthHeaders()
                }
            );


        if (!response.ok) {
            const message = await response.text();

            throw new Error(message || "배송조회에 실패했습니다.");
        }


        const data = await response.json();


        console.log("스마트택배 응답:",data);


        renderTrackingInfo(data);


    } catch (error) {

        console.error("배송조회 오류:", error);


        showTrackingError(error.message);
    }
}

function getAuthHeaders() {

    const token = localStorage.getItem("token");


    if (!token) {
        return {};
    }


    return {
        Authorization:
            `Bearer ${token}`
    };
}

function renderTrackingInfo(data) {
	const params = new URLSearchParams(window.location.search);

    const loading = document.getElementById("tracking-loading");
			
	const courier = params.get("courier");
	
	const invoice = params.get("invoice");

    const content = document.getElementById("tracking-content");


    if (loading) {
        loading.style.display = "none";
    }


    if (!content) {
        return;
    }


    if (!data || data.result !== "Y") {

        content.innerHTML = `
            <div class="tracking-error">
                배송정보를 확인할 수 없습니다.
            </div>
        `;

        return;
    }


    const details =
        Array.isArray(data.trackingDetails)
            ? [...data.trackingDetails]
            : [];


    details.sort(
        (a, b) =>
            Number(b.time || 0)
            - Number(a.time || 0)
    );


    const trackingItems =
        details
            .map(detail => `

                <div class="tracking-item">

                    <div class="tracking-dot"></div>


                    <div class="tracking-item-content">

                        <div class="tracking-item-top">

                            <strong class="tracking-kind">
                                ${escapeHtml(
                                    detail.kind || "-"
                                )}
                            </strong>

                            <span class="tracking-time">
                                ${escapeHtml(
                                    detail.timeString || "-"
                                )}
                            </span>

                        </div>


                        <div class="tracking-place">

                            ${escapeHtml(
                                detail.where || "-"
                            )}

                        </div>


                        ${
                            detail.manName

                                ? `
                                    <div class="tracking-driver">

                                        담당:
                                        ${escapeHtml(
                                            detail.manName
                                        )}

                                        ${
                                            detail.telno
                                                ? `
                                                    ·
                                                    ${escapeHtml(
                                                        detail.telno
                                                    )}
                                                `
                                                : ""
                                        }

                                    </div>
                                `

                                : ""
                        }

                    </div>

                </div>

            `)
            .join("");


    content.innerHTML = `

        <div class="tracking-summary">
		
			<div class="tracking-summary-row">
	
			    <span>
			        택배사
			    </span>
	
			    <strong>
			        ${escapeHtml(
			             courier || "-"
			        )}
			    </strong>
	
			</div>

            <div class="tracking-summary-row">

                <span>
                    운송장번호
                </span>

                <strong>
                    ${escapeHtml(
                        invoice || "-"
                    )}
                </strong>

            </div>

            <div class="tracking-summary-row">

                <span>
                    배송 여부
                </span>

                <strong>
                    ${
                        data.completeYN === "Y"
                            ? "배송 완료"
                            : "배송 중"
                    }
                </strong>

            </div>
			

        </div>


        <div class="tracking-history">

            <h2>
                배송 진행상황
            </h2>


            <div class="tracking-list">

                ${trackingItems}

            </div>

        </div>
    `;
}

function showTrackingError(message) {

    const loading = document.getElementById("tracking-loading");

    const content = document.getElementById("tracking-content");

    if (loading) {
        loading.style.display = "none";
    }

    if (content) {
        content.innerHTML = `

            <div class="tracking-error">
                ${message}
            </div>
        `;
    }
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