const params =
    new URLSearchParams(
        location.search
    );

const id =
    params.get("id");


let favoriteStatus = false;

let quantity = 1;

let currentStock = 0;


/* =====================================================
   INIT
===================================================== */

document.addEventListener(
    "DOMContentLoaded",
    () => {

        if (!id) {

            location.href =
                "/books.html";

            return;
        }


        loadBook();

        loadReviews();

        loadReviewInfo();

        loadFavoriteStatus();

    }
);


/* =====================================================
   TOKEN
===================================================== */

function getToken() {

    return (
        localStorage.getItem("token") ||
        localStorage.getItem("accessToken")
    );
}


/* =====================================================
   LOGIN REDIRECT
===================================================== */

function redirectToLogin() {

    localStorage.removeItem(
        "token"
    );

    localStorage.removeItem(
        "accessToken"
    );


    alert(
        "로그인을 해주세요."
    );


    location.href =
        "/login.html";
}


/* =====================================================
   AUTH ERROR
===================================================== */

async function handleAuthError(
    response
) {

    if (
        response.status === 401 ||
        response.status === 403
    ) {

        redirectToLogin();

        return true;
    }


    /*
     * 일부 API가 인증 실패를
     * 400으로 반환하는 경우 대응
     */
    if (
        response.status === 400
    ) {

        try {

            const clone =
                response.clone();


            let message = "";


            try {

                const data =
                    await clone.json();


                if (
                    typeof data ===
                    "string"
                ) {

                    message =
                        data;

                } else {

                    message =
                        data?.message || "";
                }


            } catch (error) {

                const text =
                    await response
                        .clone()
                        .text()
                        .catch(
                            () => ""
                        );


                message =
                    text || "";
            }


            if (
                message.includes(
                    "로그인"
                ) ||
                message.includes(
                    "인증"
                )
            ) {

                redirectToLogin();

                return true;
            }


        } catch (error) {

            console.error(
                "인증 응답 확인 오류:",
                error
            );
        }
    }


    return false;
}


/* =====================================================
   BOOK DETAIL
===================================================== */

async function loadBook() {

    try {

        const headers = {};


        if (getToken()) {

            headers.Authorization =
                "Bearer " +
                getToken();
        }


        const response =
            await fetch(
                "/books/" + id,
                {
                    headers
                }
            );


        if (!response.ok) {

            throw new Error(
                "도서 정보를 불러오지 못했습니다."
            );
        }


        const book =
            await response.json();


        /*
         * 현재 재고 저장
         */
        currentStock =
            Number(
                book.stock || 0
            );


        setText(
            "title",
            book.title
        );


        setText(
            "breadcrumbTitle",
            book.title
        );


        setText(
            "author",
            book.author || "-"
        );


        setText(
            "publisher",
            book.publisher || "-"
        );


        setText(
            "category",
            book.category || "BOOK"
        );


        setText(
            "categoryText",
            book.category || "-"
        );


        setText(
            "stock",
            book.stock ?? 0
        );


        setText(
            "description",
            book.description ||
            "도서 소개가 없습니다."
        );


        setText(
            "isbn",
            book.isbn || "-"
        );


        setText(
            "publishedDate",
            book.publishedDate || "-"
        );


        const price =
            Number(
                book.price ?? 0
            );


        const salePrice =
            Number(
                book.salePrice ??
                book.price ??
                0
            );


        const discountRate =
            Number(
                book.discountRate ||
                0
            );


        setText(
            "salePrice",
            salePrice.toLocaleString()
        );


        setText(
            "discountRate",
            discountRate > 0
                ? discountRate + "%"
                : ""
        );


        setText(
            "originalPrice",
            discountRate > 0
                ? price.toLocaleString()
                + "원"
                : ""
        );


        const image =
            document.getElementById(
                "thumbnail"
            );


        if (image) {

            image.src =
                book.thumbnail || "";


            image.onerror =
                () => {

                    image.style.display =
                        "none";
                };
        }


        /*
         * 품절 처리
         */
        const cartButton =
            document.getElementById(
                "cartButton"
            );


        const buyButton =
            document.getElementById(
                "buyButton"
            );


        if (
            Number(
                book.stock || 0
            ) <= 0
        ) {

            if (cartButton) {

                cartButton.disabled =
                    true;

                cartButton.textContent =
                    "품절";
            }


            if (buyButton) {

                buyButton.disabled =
                    true;

                buyButton.textContent =
                    "품절";
            }
        }


        /*
         * 연관 공연 조회
         */
        loadLinkedPerformance(
            book.id,
            book.title
        );


    } catch (error) {

        setText(
            "title",
            "도서 정보를 불러오지 못했습니다."
        );


        console.error(
            "도서 상세 조회 오류:",
            error
        );
    }
}


/* =====================================================
   QUANTITY
===================================================== */

function increaseQuantity() {

    if (
        currentStock <= 0
    ) {

        showToast(
            "품절된 도서입니다."
        );

        return;
    }


    if (
        quantity >=
        currentStock
    ) {

        showToast(
            "재고 수량을 초과할 수 없습니다."
        );

        return;
    }


    quantity++;


    updateQuantity();
}


function decreaseQuantity() {

    if (
        quantity <= 1
    ) {

        return;
    }


    quantity--;


    updateQuantity();
}


function updateQuantity() {

    const element =
        document.getElementById(
            "quantityValue"
        );


    if (element) {

        element.textContent =
            quantity;
    }
}


/* =====================================================
   LINKED PERFORMANCE
===================================================== */

async function loadLinkedPerformance(
    bookId,
    bookTitle
) {

    try {

        const response =
            await fetch(
                "/api/performances"
            );


        if (!response.ok) {

            return;
        }


        const result =
            await response.json();


        const performances =
            result &&
            Array.isArray(
                result.data
            )
                ? result.data
                : [];


        /*
         * DB originalBookId 기반 도서-공연 1:1 정규 연동
         */
        const linkedPerformance = performances.find(
            performance => performance.originalBookId && String(performance.originalBookId) === String(bookId)
        );


        if (!linkedPerformance) {

            return;
        }


        const box =
            document.getElementById(
                "linkedPerformanceBox"
            );


        const titleElement =
            document.getElementById(
                "linkedPerfTitle"
            );


        const venueElement =
            document.getElementById(
                "linkedPerfVenue"
            );


        const button =
            document.getElementById(
                "linkedPerfBtn"
            );


        if (
            !box ||
            !titleElement ||
            !venueElement ||
            !button
        ) {

            return;
        }


        titleElement.textContent =
            linkedPerformance.title;


        venueElement.textContent =
            `공연장: ${
                linkedPerformance.venue ||
                "장소 미정"
            } (${
                linkedPerformance
                    .runtimeMinutes ||
                150
            }분)`;


        button.onclick =
            () => {

                location.href =
                    "/detail.html?id=" +
                    encodeURIComponent(
                        linkedPerformance.id
                    );
            };


        box.style.display =
            "block";


    } catch (error) {

        console.error(
            "연관 공연 조회 실패:",
            error
        );
    }
}


/* =====================================================
   FAVORITE STATUS
===================================================== */

async function loadFavoriteStatus() {

    /*
     * 비회원도 도서 상세페이지는
     * 볼 수 있어야 하므로
     * 여기서는 로그인 페이지로 이동하지 않음
     */
    if (!getToken()) {

        favoriteStatus =
            false;


        updateFavoriteButton();


        return;
    }


    try {

        const response =
            await fetch(
                "/members/favorites",
                {
                    headers: {

                        Authorization:
                            "Bearer " +
                            getToken()
                    }
                }
            );


        if (
            await handleAuthError(
                response
            )
        ) {

            return;
        }


        if (!response.ok) {

            return;
        }


        const favorites =
            await response.json();


        favoriteStatus =
            Array.isArray(
                favorites
            ) &&
            favorites.some(
                book =>

                    String(
                        book.id
                    ) ===
                    String(
                        id
                    )
            );


        updateFavoriteButton();


    } catch (error) {

        console.error(
            "관심 도서 상태 조회 오류:",
            error
        );
    }
}


/* =====================================================
   FAVORITE
===================================================== */

async function favorite() {

    if (!getToken()) {

        redirectToLogin();

        return;
    }


    try {

        const method =
            favoriteStatus
                ? "DELETE"
                : "POST";


        const response =
            await fetch(
                "/members/favorites/" +
                encodeURIComponent(
                    id
                ),
                {
                    method,

                    headers: {

                        Authorization:
                            "Bearer " +
                            getToken()
                    }
                }
            );


        if (
            await handleAuthError(
                response
            )
        ) {

            return;
        }


        if (!response.ok) {

            let message =
                "관심 도서 처리에 실패했습니다.";


            try {

                const data =
                    await response.json();


                message =
                    data?.message ||
                    message;


            } catch (error) {

                const text =
                    await response
                        .text()
                        .catch(
                            () => ""
                        );


                if (text) {

                    message =
                        text;
                }
            }


            throw new Error(
                message
            );
        }


        favoriteStatus =
            !favoriteStatus;


        updateFavoriteButton();


    } catch (error) {

        alert(
            error.message ||
            "관심 도서 처리에 실패했습니다."
        );
    }
}


/* =====================================================
   FAVORITE BUTTON
===================================================== */

function updateFavoriteButton() {

    const button =
        document.getElementById(
            "favoriteButton"
        );


    if (!button) {

        return;
    }


    button.textContent =
        favoriteStatus
            ? "♥ 관심 도서"
            : "♡ 관심 도서";


    button.classList.toggle(
        "active",
        favoriteStatus
    );
}


/* =====================================================
   CART
===================================================== */

async function cart() {

    if (!getToken()) {

        redirectToLogin();

        return;
    }


    if (
        currentStock <= 0
    ) {

        showToast(
            "품절된 도서입니다."
        );

        return;
    }


    try {

        const response =
            await fetch(
                "/api/cart/items",
                {
                    method:
                        "POST",

                    headers: {

                        "Content-Type":
                            "application/x-www-form-urlencoded",

                        Authorization:
                            "Bearer " +
                            getToken()
                    },

                    body:
                        "bookId=" +
                        encodeURIComponent(
                            id
                        ) +
                        "&quantity=" +
                        encodeURIComponent(
                            quantity
                        )
                }
            );


        if (
            await handleAuthError(
                response
            )
        ) {

            return;
        }


        const result =
            await response
                .json()
                .catch(
                    () => null
                );


        if (!response.ok) {

            throw new Error(
                result?.message ||
                "장바구니 추가에 실패했습니다."
            );
        }


        showToast(
            "장바구니에 추가했습니다."
        );


    } catch (error) {

        showToast(
            error.message ||
            "장바구니 추가에 실패했습니다."
        );
    }
}


/* =====================================================
   BUY NOW
===================================================== */

async function buyNow() {

    if (!getToken()) {

        redirectToLogin();

        return;
    }


    if (
        currentStock <= 0
    ) {

        alert(
            "품절된 도서입니다."
        );

        return;
    }


    try {

        /*
         * 현재 주문 API가 cartItemIds 기반이라
         * 구매하기 시 장바구니에 추가 후
         * 장바구니 페이지로 이동
         */
        const response =
            await fetch(
                "/api/cart/items",
                {
                    method:
                        "POST",

                    headers: {

                        "Content-Type":
                            "application/x-www-form-urlencoded",

                        Authorization:
                            "Bearer " +
                            getToken()
                    },

                    body:
                        "bookId=" +
                        encodeURIComponent(
                            id
                        ) +
                        "&quantity=" +
                        encodeURIComponent(
                            quantity
                        )
                }
            );


        if (
            await handleAuthError(
                response
            )
        ) {

            return;
        }


        const result =
            await response
                .json()
                .catch(
                    () => null
                );


        if (!response.ok) {

            throw new Error(
                result?.message ||
                "구매 처리에 실패했습니다."
            );
        }


        location.href =
            "/cart";


    } catch (error) {

        alert(
            error.message ||
            "구매 처리에 실패했습니다."
        );
    }
}


/* =====================================================
   TOAST
===================================================== */

function showToast(
    message
) {

    const existingToast =
        document.querySelector(
            ".toast-message"
        );


    if (existingToast) {

        existingToast.remove();
    }


    const toast =
        document.createElement(
            "div"
        );


    toast.className =
        "toast-message";


    toast.textContent =
        message;


    document.body.appendChild(
        toast
    );


    setTimeout(
        () => {

            toast.classList.add(
                "show"
            );

        },
        10
    );


    setTimeout(
        () => {

            toast.classList.remove(
                "show"
            );


            setTimeout(
                () => {

                    toast.remove();

                },
                300
            );

        },
        2000
    );
}


/* =====================================================
   JWT EMAIL
===================================================== */

function getMyEmail() {

    const token =
        getToken();


    if (!token) {

        return null;
    }


    try {

        const payload =
            token
                .split(".")[1];


        if (!payload) {

            return null;
        }


        const normalized =
            payload
                .replace(
                    /-/g,
                    "+"
                )
                .replace(
                    /_/g,
                    "/"
                );


        const decoded =
            JSON.parse(
                atob(
                    normalized
                )
            );


        return (
            decoded.sub ||
            decoded.email ||
            null
        );


    } catch (error) {

        return null;
    }
}


/* =====================================================
   REVIEWS
===================================================== */

async function loadReviews() {

    const container =
        document.getElementById(
            "reviewList"
        );


    if (!container) {

        return;
    }


    try {

        const response =
            await fetch(
                `/books/${id}/reviews`
            );


        if (!response.ok) {

            throw new Error(
                "리뷰 조회 실패"
            );
        }


        const reviews =
            await response.json();


        if (
            !Array.isArray(
                reviews
            ) ||
            reviews.length === 0
        ) {

            container.innerHTML = `

                <div
                    class="bk-empty-state compact"
                >
                    <p>
                        아직 작성된 리뷰가 없습니다.
                    </p>
                </div>

            `;


            return;
        }


        const myEmail =
            getMyEmail();


        container.innerHTML =
            reviews
                .map(
                    review =>
                        createReviewHtml(
                            review,
                            myEmail
                        )
                )
                .join("");


    } catch (error) {

        container.innerHTML = `

            <p class="bk-error-text">
                리뷰를 불러오지 못했습니다.
            </p>

        `;


        console.error(
            "리뷰 조회 오류:",
            error
        );
    }
}


/* =====================================================
   REVIEW HTML
===================================================== */

function createReviewHtml(
    review,
    myEmail
) {

    const rating =
        Math.max(
            0,
            Math.min(
                5,
                Number(
                    review.rating ||
                    0
                )
            )
        );


    const deleteButton =
        myEmail &&
        myEmail ===
        review.email

            ? `

                <button
                    class="bk-danger-link"
                    type="button"
                    onclick="deleteReview(${review.id})"
                >
                    삭제
                </button>

            `

            : "";


    return `

        <article class="bk-review-item">

            <div class="bk-review-item-head">

                <div>

                    <strong>
                        ${
        escapeHtml(
            review.name ||
            "회원"
        )
    }
                    </strong>

                    <span class="bk-stars">

                        ${"★".repeat(rating)}
                        ${"☆".repeat(5 - rating)}

                    </span>

                </div>

                ${deleteButton}

            </div>


            <p>
                ${
        escapeHtml(
            review.content ||
            ""
        )
    }
            </p>


            <time>
                ${
        formatDate(
            review.createdAt
        )
    }
            </time>

        </article>

    `;
}


/* =====================================================
   SAVE REVIEW
===================================================== */

async function saveReview() {

    if (!getToken()) {

        redirectToLogin();

        return;
    }


    const contentElement =
        document.getElementById(
            "reviewContent"
        );


    const ratingElement =
        document.getElementById(
            "rating"
        );


    if (
        !contentElement ||
        !ratingElement
    ) {

        alert(
            "리뷰 입력 정보를 확인할 수 없습니다."
        );

        return;
    }


    const content =
        contentElement
            .value
            .trim();


    const rating =
        Number(
            ratingElement.value
        );


    if (!content) {

        alert(
            "리뷰 내용을 입력해주세요."
        );

        return;
    }


    if (
        !Number.isInteger(
            rating
        ) ||
        rating < 1 ||
        rating > 5
    ) {

        alert(
            "평점을 선택해주세요."
        );

        return;
    }


    const button =
        document.getElementById(
            "reviewSubmitButton"
        );


    if (button) {

        button.disabled =
            true;

        button.textContent =
            "등록 중...";
    }


    try {

        const response =
            await fetch(
                `/books/${id}/reviews`,
                {
                    method:
                        "POST",

                    headers: {

                        "Content-Type":
                            "application/json",

                        Authorization:
                            "Bearer " +
                            getToken()
                    },

                    body:
                        JSON.stringify(
                            {
                                content,
                                rating
                            }
                        )
                }
            );


        if (
            await handleAuthError(
                response
            )
        ) {

            return;
        }


        const data =
            await response
                .json()
                .catch(
                    () => ({})
                );


        if (!response.ok) {

            throw new Error(
                data.message ||
                "리뷰 등록에 실패했습니다."
            );
        }


        contentElement.value =
            "";


        await Promise.all(
            [
                loadReviews(),
                loadReviewInfo()
            ]
        );


    } catch (error) {

        alert(
            error.message ||
            "리뷰 등록에 실패했습니다."
        );


    } finally {

        if (button) {

            button.disabled =
                false;

            button.textContent =
                "리뷰 등록";
        }
    }
}


/* =====================================================
   DELETE REVIEW
===================================================== */

async function deleteReview(
    reviewId
) {

    if (!getToken()) {

        redirectToLogin();

        return;
    }


    if (
        !confirm(
            "리뷰를 삭제하시겠습니까?"
        )
    ) {

        return;
    }


    try {

        const response =
            await fetch(
                "/reviews/" +
                encodeURIComponent(
                    reviewId
                ),
                {
                    method:
                        "DELETE",

                    headers: {

                        Authorization:
                            "Bearer " +
                            getToken()
                    }
                }
            );


        if (
            await handleAuthError(
                response
            )
        ) {

            return;
        }


        if (!response.ok) {

            let message =
                "리뷰 삭제에 실패했습니다.";


            try {

                const data =
                    await response.json();


                message =
                    data?.message ||
                    message;


            } catch (error) {

                const text =
                    await response
                        .text()
                        .catch(
                            () => ""
                        );


                if (text) {

                    message =
                        text;
                }
            }


            throw new Error(
                message
            );
        }


        await Promise.all(
            [
                loadReviews(),
                loadReviewInfo()
            ]
        );


    } catch (error) {

        alert(
            error.message ||
            "리뷰 삭제에 실패했습니다."
        );
    }
}


/* =====================================================
   REVIEW INFO
===================================================== */

async function loadReviewInfo() {

    try {

        const response =
            await fetch(
                `/books/${id}/reviews/info`
            );


        if (!response.ok) {

            throw new Error(
                "리뷰 정보를 불러오지 못했습니다."
            );
        }


        const info =
            await response.json();


        const average =
            Number(
                info.averageRating ||
                0
            ).toFixed(1);


        const count =
            Number(
                info.reviewCount ||
                0
            );


        setText(
            "averageRating",
            average
        );


        setText(
            "reviewCount",
            `리뷰 ${count}개`
        );


        const reviewInfo =
            document.getElementById(
                "reviewInfo"
            );


        if (reviewInfo) {

            reviewInfo.textContent =
                `★ ${average} · ${count}개 리뷰`;
        }


    } catch (error) {

        console.error(
            "리뷰 정보 조회 오류:",
            error
        );
    }
}


/* =====================================================
   SET TEXT
===================================================== */

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


/* =====================================================
   FORMAT DATE
===================================================== */

function formatDate(
    value
) {

    if (!value) {

        return "";
    }


    const date =
        new Date(
            value
        );


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return value;
    }


    return date
        .toLocaleDateString(
            "ko-KR"
        );
}


/* =====================================================
   ESCAPE HTML
===================================================== */

function escapeHtml(
    value
) {

    return String(
        value ?? ""
    )
        .replaceAll(
            "&",
            "&amp;"
        )
        .replaceAll(
            "<",
            "&lt;"
        )
        .replaceAll(
            ">",
            "&gt;"
        )
        .replaceAll(
            '"',
            "&quot;"
        )
        .replaceAll(
            "'",
            "&#039;"
        );
}