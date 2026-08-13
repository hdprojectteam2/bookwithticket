window.onload = function() {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "/login.html";
        return;
    }

    loadBookCart();
    loadPerformanceCart();

    const openPerformanceCart = localStorage.getItem("openPerformanceCart");

    if (openPerformanceCart === "true") {
        cartTab("ticketCart");
        localStorage.removeItem("openPerformanceCart");
    } else {
        cartTab("bookCart");
    }
};


function cartTab(tabId) {
    const bookCart = document.getElementById("bookCart");

    const ticketCart = document.getElementById("ticketCart");

    bookCart.hidden = tabId !== "bookCart";

    ticketCart.hidden = tabId !== "ticketCart";
}


async function loadBookCart() {
    const box =
        document.getElementById("bookCart");

    try {
        const response =
            await fetch("/api/cart", {
                headers: getAuthHeaders()
            });

        if (!response.ok) {
            throw new Error(
                "도서 장바구니 조회에 실패했습니다."
            );
        }

        const items =
            await response.json();

			if (items.length === 0) {
				box.innerHTML = `
					<section class="cart-panel">
						<div class="cart-header">
							<div class="cart-title">
						    	도서 장바구니
						        <span>(0개)</span>
						    </div>
						</div>
						<div class="empty-cart">
							공연 장바구니가 비어있습니다.
						</div>
					</section>
					`;

			    return;
			}

        box.innerHTML = `
            <div class="cart-layout">

                <section class="cart-left card">

                    <div class="cart-header">

                        <div class="cart-title">
                            도서 장바구니
                            <span>
                                (${items.length}종)
                            </span>
                        </div>

                        <button
                            type="button"
                            class="delete-selected-button"
                            onclick="deleteAllItems()">
                            전체 삭제
                        </button>

                    </div>


                    <div class="select-all-row">
	
						<div>
                            <input
                                type="checkbox"
                                id="selectAll"
                                onchange="toggleSelectAll(this)">
                            전체 선택
						</div>

                        <span
                            id="selectedCountText"
                            class="selected-count">
                        </span>

                    </div>


                    <div id="bookCartItems">

                        ${items
                .map(item =>
                    createBookCartItem(item)
                )
                .join("")}

                    </div>

                </section>


                ${createBookCartSummary()}

            </div>
        `;

        updateSelectedSummary();

    } catch (error) {
        console.error(
            "도서 장바구니 조회 오류:",
            error
        );

        box.innerHTML = `
            <div class="empty-cart">
                장바구니를 불러오지 못했습니다.
            </div>
        `;
    }
}


function createBookCartItem(item) {

    const purchasable = item.purchasable === true;

    return `
        <div
            class="cart-item
                ${purchasable ? "" : "unavailable"}"
            data-price="${item.price}">

            <input
                type="checkbox"
                class="item-checkbox"
                value="${item.cartItemId}"

                ${purchasable
            ? "checked"
            : "disabled"
        }

                onchange="updateSelectedSummary()"
            >


            <img
                class="cart-book-image"
                src="${item.thumbnail}"
                alt="${item.bookTitle}"
            >


            <div class="cart-item-info">

                <h3 class="cart-book-title">
                    ${item.bookTitle}
                </h3>


                <div class="cart-book-detail">

                    <span>
                        ${item.author || ""}
                    </span>

                    <span>·</span>

                    <span>
                        ${item.publisher || ""}
                    </span>

                </div>


                ${purchasable
            ? createQuantityBox(item)
            : createUnavailableStatus(item)
        }


                <div class="cart-unit-price">
                    ${formatPrice(item.price)}원
                </div>

            </div>


            <div class="cart-item-price">

                ${formatPrice(
            item.price * item.quantity
        )}원

            </div>


            <button
                type="button"
                class="cart-delete-button"
                onclick="
                    deleteCartItem(
                        ${item.cartItemId}
                    )
                "
            >
                ×
            </button>

        </div>
    `;
}


function createQuantityBox(item) {

    return `
        <div class="quantityBox">

            <button
                type="button"
                onclick="
                    changeQuantity(
                        this,
                        -1
                    )
                "
            >
                -
            </button>


            <input
                type="number"
                min="1"
                max="99"

                value="${item.quantity}"

                data-cart-item-id="${item.cartItemId}"

                data-stock="${item.stock}"

                onchange="updateQuantity(this)"
            >


            <button
                type="button"
                onclick="
                    changeQuantity(
                        this,
                        1
                    )
                "
            >
                +
            </button>


            <span class="stock-text">
                재고 ${item.stock}개
            </span>

        </div>
    `;
}


function createUnavailableStatus(item) {

    return `
        <div class="unavailable-status">

            <span class="sold-out-badge">
                품절
            </span>

            <span>
                ${item.unavailableReason || "현재 구매 불가"}
            </span>

        </div>
    `;
}



function createBookCartSummary() {

    return `
        <aside class="cart-summary">

            <h2>
                도서 주문 요약
            </h2>


            <div class="summary-row">

                <span>
                    선택 수량
                </span>

                <span>
                    <strong
                        id="selectTotalQuantity">
                        0
                    </strong>개
                </span>

            </div>


            <div class="summary-row">

                <span>
                    상품 금액
                </span>

                <span>
                    <strong
                        id="selectTotalPrice">
                        0
                    </strong>원
                </span>

            </div>


            <hr>


            <div class="summary-total">

                <span>
                    최종 결제 금액
                </span>

                <strong>
                    <span
                        id="finalTotalPrice">
                        0
                    </span>원
                </strong>

            </div>
			

            <button
                type="button"
                class="order-button"
                onclick="orderDelivery()">
                도서 주문하기
            </button>
			
			<hr>
						
			<div>
				<div class="summary-row">
				
					<span id="summaryText">
						선택된 도서만 주문됩니다. 품절 상품은 제외됩니다.
					</span>
	
				</div>
			</div>

        </aside>
    `;
}



function toggleSelectAll(selectAll) {

    const checkboxes = document.querySelectorAll("#bookCart .item-checkbox");

    checkboxes.forEach(checkbox => {

        if (checkbox.disabled) {
            return;
        }

        checkbox.checked = selectAll.checked;
    });

    updateSelectedSummary();
}



function updateSelectedSummary() {

    const cartItems = document.querySelectorAll("#bookCart .cart-item");

    let selectedItemCount = 0;
    let selectedTotalQuantity = 0;
    let selectedTotalPrice = 0;


    cartItems.forEach(cartItem => {

        const checkbox = cartItem.querySelector(".item-checkbox");

        if (!checkbox ||
            checkbox.disabled ||
            !checkbox.checked
        ) {
            return;
        }


        const quantityInput =
            cartItem.querySelector(
                ".quantityBox input"
            );

        if (!quantityInput) {
            return;
        }


        const quantity = Number(quantityInput.value);

        const price = Number(cartItem.dataset.price);


        selectedItemCount++;

        selectedTotalQuantity += quantity;

        selectedTotalPrice += price * quantity;
    });


    const quantityElement = document.getElementById("selectTotalQuantity");

    const priceElement = document.getElementById("selectTotalPrice");

    const finalPriceElement = document.getElementById("finalTotalPrice");

    const countElement = document.getElementById("selectedCountText");


    if (quantityElement) {
        quantityElement.textContent = selectedTotalQuantity;
    }


    if (priceElement) {
        priceElement.textContent = formatPrice(selectedTotalPrice);
    }


    if (finalPriceElement) {
        finalPriceElement.textContent = formatPrice(selectedTotalPrice);
    }


    if (countElement) {
        countElement.textContent = `(${selectedItemCount}개 선택)`;
    }


    updateSelectAllCheckbox();
}



function updateSelectAllCheckbox() {

    const selectAll = document.getElementById("selectAll");

    if (!selectAll) {
        return;
    }


    const enabledCheckboxes =
        Array.from(
            document.querySelectorAll(
                "#bookCart .item-checkbox:not(:disabled)"
            )
        );


    if (enabledCheckboxes.length === 0) {

        selectAll.checked = false;

        return;
    }


    selectAll.checked =
        enabledCheckboxes.every(
            checkbox =>
                checkbox.checked
        );
}



function changeQuantity(button, amount) {

    const quantityBox = button.closest(".quantityBox");

    const input = quantityBox.querySelector("input");


    const currentQuantity = Number(input.value);

    const stockQuantity = Number(input.dataset.stock);

    const maxQuantity = 99;

    const newQuantity = currentQuantity + amount;


    if (newQuantity < 1) {

        alert("수량은 1개 이상이어야 합니다.");

        return;
    }


    if (newQuantity > stockQuantity) {

        alert("현재 재고는 " + stockQuantity + "개입니다.");

        return;
    }


    if (newQuantity > maxQuantity) {

        alert("최대 " + maxQuantity + "개까지 구매할 수 있습니다."
        );

        return;
    }


    input.value = newQuantity;

    updateQuantity(input);
}


async function updateQuantity(input) {

    const cartItemId = input.dataset.cartItemId;

    const quantity = Number(input.value);

    const stockQuantity = Number(input.dataset.stock);

    const maxQuantity = 99;


    if (!Number.isInteger(quantity) || quantity < 1) {

        alert("수량은 1 이상의 정수만 입력할 수 있습니다.");

        await loadBookCart();

        return;
    }


    if (quantity > stockQuantity) {

        alert("현재 재고는 " + stockQuantity + "개입니다.");

        await loadBookCart();

        return;
    }


    if (quantity > maxQuantity) {

        alert("최대 " + maxQuantity + "개까지 구매할 수 있습니다.");

        await loadBookCart();

        return;
    }


    try {

        const params = new URLSearchParams();

        params.append("quantity", quantity);


        const response =
            await fetch(
                "/api/cart/items/" + cartItemId,
                {
                    method: "PATCH",

                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        ...getAuthHeaders()
                    },

                    body: params
                }
            );


        const message = await response.text();


        if (!response.ok) {

            throw new Error(message || "수량 변경에 실패했습니다.");
        }


        await loadBookCart();


    } catch (error) {

        console.error("수량 변경 오류:", error);

        alert(error.message);

        await loadBookCart();
    }
}



async function deleteCartItem(cartItemId) {

    try {

        const response =
            await fetch(
                "/api/cart/items/" + cartItemId,
                {
                    method: "DELETE",
                    headers: getAuthHeaders()
                }
            );


        if (!response.ok) {

            const message = await response.text();

            throw new Error(message || "삭제에 실패했습니다.");
        }


        await loadBookCart();


    } catch (error) {

        console.error("장바구니 삭제 오류:", error);

        alert(error.message);
    }
}


async function deleteAllItems() {

    if (!confirm("도서 장바구니를 모두 삭제하시겠습니까?")) {
        return;
    }


    try {

        const response =
            await fetch(
                "/api/cart/items",
                {
                    method: "DELETE",
                    headers: getAuthHeaders()
                }
            );


        const message = await response.text();


        if (!response.ok) {

            throw new Error(message || "장바구니 전체 삭제에 실패했습니다.");
        }


        await loadBookCart();


    } catch (error) {

        console.error("장바구니 전체 삭제 오류:", error);

        alert(error.message);
    }
}


async function orderDelivery() {

    const checkedItems = document.querySelectorAll("#bookCart .item-checkbox:checked:not(:disabled)");


    if (checkedItems.length === 0) {

        alert("주문할 상품을 선택해주세요.");

        return;
    }


    const cartItemIds =
        Array.from(
            checkedItems
        ).map(
            checkbox =>
                Number(
                    checkbox.value
                )
        );


    try {

        const response =
            await fetch(
                "/api/orders/prepare",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
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

            throw new Error(message || "주문 준비에 실패했습니다.");
        }


        const order = await response.json();


        location.href =
            "/order?orderNumber="
            + encodeURIComponent(
                order.orderNumber
            );


    } catch (error) {

        console.error("주문 준비 오류:", error);

        alert(error.message);
    }
}


async function loadPerformanceCart() {

    const box = document.getElementById("ticketCart");


    try {

        const response =
            await fetch(
                "/api/cart/performances",
                {
                    headers: getAuthHeaders()
                }
            );


        if (!response.ok) {

            throw new Error("공연 장바구니 조회에 실패했습니다.");
        }


        const items = await response.json();


		if (items.length === 0) {
		    box.innerHTML = `
		        <section class="cart-panel">
		            <div class="cart-header">
		                <div class="cart-title">
		                    티켓 장바구니
		                    <span>(0개)</span>
		                </div>
		            </div>
		            <div class="empty-cart">
		                공연 장바구니가 비어있습니다.
		            </div>
		        </section>
		    `;

		    return;
		}


        box.innerHTML = `
            <section class="cart-left card">

                <div class="cart-header">

                    <div class="cart-title">
                        티켓 장바구니
                        <span>
                            (${items.length}개)
                        </span>
                    </div>

                    <button
                        type="button"
                        onclick="
                            deleteAllPerformanceItems()
                        "
                    >
                        전체 삭제
                    </button>

                </div>


                <div
                    class="performance-cart-list"
                >

                    ${items
                .map(item =>
                    createPerformanceCartItem(
                        item
                    )
                )
                .join("")}

                </div>

            </section>
        `;


    } catch (error) {

        console.error(
            "공연 장바구니 조회 오류:",
            error
        );


        box.innerHTML = `
            <div class="empty-cart">
                공연 장바구니를 불러오지 못했습니다.
            </div>
        `;
    }
}


function createPerformanceCartItem(item) {

    return `
        <div class="performance-cart-item">

            <img
                class="performance-cart-image"
                src="${item.posterUrl || ""}"
                alt="${item.title}"
            >


            <div class="performance-cart-info">

                <h3>
                    ${item.title}
                </h3>


                <p>
                    ${item.venue}
                </p>


                <p>
                    공연 일시
                    ${formatDateTime(
        item.performanceTime
    )}
                </p>


                <p>
                    예매 시작
                    ${formatDateTime(
        item.ticketOpenTime
    )}
                </p>


                ${createPerformanceButton(item)}

            </div>


            <button
                type="button"
                class="cart-delete-button"
                onclick="
                    deletePerformanceCartItem(
                        ${item.cartItemId}
                    )
                "
            >
                ×
            </button>

        </div>
    `;
}


function createPerformanceButton(item) {

    if (
        item.status ===
        "OPEN_SCHEDULED"
    ) {

        return `
            <button
                type="button"
                disabled
            >
                오픈 예정
            </button>
        `;
    }


    if (
        item.status ===
        "AVAILABLE"
    ) {

        return `
            <button
                type="button"
                onclick="
                    moveToPerformance(
                        ${item.performanceId},
                        ${item.scheduleId}
                    )
                "
            >
                상세보기
            </button>
        `;
    }


    if (
        item.status ===
        "EXPIRED"
    ) {

        return `
            <button
                type="button"
                disabled
            >
                종료된 공연
            </button>
        `;
    }


    return "";
}


function moveToPerformance(performanceId, scheduleId) {

    location.href = "/detail.html?id=" + encodeURIComponent(performanceId);
}


async function deletePerformanceCartItem(
    itemId
) {

    try {

        const response =
            await fetch(
                "/api/cart/performances/" + itemId,
                {
                    method: "DELETE",
                    headers: getAuthHeaders()
                }
            );


        if (!response.ok) {

            throw new Error(
                "삭제에 실패했습니다."
            );
        }


        await loadPerformanceCart();


    } catch (error) {

        console.error(
            "공연 장바구니 삭제 오류:",
            error
        );

        alert(error.message);
    }
}


async function deleteAllPerformanceItems() {

    if (
        !confirm(
            "공연 장바구니를 모두 삭제하시겠습니까?"
        )
    ) {
        return;
    }


    try {

        const response =
            await fetch(
                "/api/cart/performances",
                {
                    method: "DELETE",
                    headers: getAuthHeaders()
                }
            );


        const message = await response.text();


        if (!response.ok) {

            throw new Error(message || "장바구니 전체 삭제에 실패했습니다.");
        }


        await loadPerformanceCart();


    } catch (error) {

        console.error("공연 장바구니 전체 삭제 오류:", error);

        alert(error.message);
    }
}


function formatPrice(price) {

    return Number(price)
        .toLocaleString("ko-KR");
}


function formatDateTime(dateTime) {

    if (!dateTime) {
        return "";
    }


    const date = new Date(dateTime);


    const year = date.getFullYear();

    const month =
        String(
            date.getMonth() + 1
        ).padStart(
            2,
            "0"
        );

    const day =
        String(
            date.getDate()
        ).padStart(
            2,
            "0"
        );

    const hour =
        String(
            date.getHours()
        ).padStart(
            2,
            "0"
        );

    const minute =
        String(
            date.getMinutes()
        ).padStart(
            2,
            "0"
        );


    return (
        `${year}.${month}.${day} `
        + `${hour}:${minute}`
    );
}

function getAuthHeaders() {

    const token =
        localStorage.getItem("token");

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