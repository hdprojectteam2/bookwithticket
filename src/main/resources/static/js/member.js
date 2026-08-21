/* =====================================================
   INIT
===================================================== */

document.addEventListener(
    "DOMContentLoaded",
    () => {

        getMyInfo();
        loadRecentBooks();
        loadFavoriteBooks();
        loadCartBooks();
        loadMyReviews();

    }
);


/* =====================================================
   AUTH
===================================================== */

function getToken() {

    return localStorage.getItem(
        "token"
    );

}


function authHeaders() {

    return {
        Authorization:
            "Bearer " + getToken()
    };

}


/* =====================================================
   MEMBER INFO
===================================================== */

async function getMyInfo() {

    if (!getToken()) {

        location.href =
            "/login.html";

        return;

    }


    try {

        const response =
            await fetch(
                "/members/me",
                {
                    headers:
                        authHeaders()
                }
            );


        if (!response.ok) {

            throw new Error(
                "로그인이 필요합니다."
            );

        }


        const member =
            await response.json();


        setText(
            "memberName",
            member.name || ""
        );


        setText(
            "email",
            member.email || "-"
        );


        setText(
            "name",
            member.name || "-"
        );


        setText(
            "phone",
            member.phone || "-"
        );


        const address =
            [
                member.zipcode,
                member.address,
                member.detailAddress
            ]
                .filter(Boolean)
                .join(" ");


        setText(
            "address",
            address || "-"
        );


    } catch (error) {

        localStorage.removeItem(
            "token"
        );


        alert(
            error.message
        );


        location.href =
            "/login.html";

    }

}


/* =====================================================
   RECENT BOOKS
===================================================== */

async function loadRecentBooks() {

    await renderBookActivity(
        "/members/recent-books",
        "recentBooks",
        "최근 본 도서가 없습니다."
    );

}


/* =====================================================
   FAVORITE BOOKS
===================================================== */

async function loadFavoriteBooks() {

    await renderBookActivity(
        "/members/favorites",
        "favoriteBooks",
        "관심 도서가 없습니다."
    );

}


/* =====================================================
   CART BOOKS
===================================================== */

async function loadCartBooks() {

    const container =
        document.getElementById(
            "cartBooks"
        );


    if (!container) {
        return;
    }


    try {

        const response =
            await fetch(
                "/api/cart",
                {
                    headers:
                        authHeaders()
                }
            );


        if (!response.ok) {

            throw new Error(
                "장바구니 조회 실패"
            );

        }


        const items =
            await response.json();


        if (
            !Array.isArray(items) ||
            items.length === 0
        ) {

            container.innerHTML = `

                <div class="bk-empty-state compact">

                    <p>
                        장바구니에 담긴 도서가 없습니다.
                    </p>

                    <a href="/books.html">
                        도서 둘러보기
                    </a>

                </div>

            `;

            return;

        }


        container.innerHTML =
            items
                .slice(0, 6)
                .map(
                    createCartBookCard
                )
                .join("");


    } catch (error) {

        console.error(error);


        container.innerHTML = `

            <p class="bk-error-text">
                장바구니를 불러오지 못했습니다.
            </p>

        `;

    }

}


/* =====================================================
   CART BOOK CARD
===================================================== */

function createCartBookCard(item) {

    const title =
        escapeHtml(
            item.bookTitle ||
            item.title ||
            "도서"
        );


    const author =
        escapeHtml(
            item.author || ""
        );


    const thumbnail =
        escapeHtml(
            item.thumbnail || ""
        );


    const price =
        Number(
            item.price ??
            item.salePrice ??
            0
        );


    const quantity =
        Number(
            item.quantity || 1
        );


    const bookId =
        item.bookId ??
        item.id;


    return `

        <article
            class="bk-mini-book"
            onclick="goBook(${bookId})"
        >

            <img
                src="${thumbnail}"
                alt="${title}"
                onerror="this.style.visibility='hidden'"
            >


            <div>

                <h4>
                    ${title}
                </h4>

                <p>
                    ${author}
                </p>

                <strong>
                    ${price.toLocaleString()}원
                    · ${quantity}개
                </strong>

            </div>

        </article>

    `;

}


/* =====================================================
   COMMON BOOK ACTIVITY
===================================================== */

async function renderBookActivity(
    url,
    targetId,
    emptyMessage
) {

    const container =
        document.getElementById(
            targetId
        );


    if (!container) {
        return;
    }


    try {

        const response =
            await fetch(
                url,
                {
                    headers:
                        authHeaders()
                }
            );


        if (!response.ok) {

            throw new Error(
                "도서 정보 조회 실패"
            );

        }


        const books =
            await response.json();


        if (
            !Array.isArray(books) ||
            books.length === 0
        ) {

            container.innerHTML = `

                <div class="bk-empty-state compact">

                    <p>
                        ${escapeHtml(
                emptyMessage
            )}
                    </p>

                    <a href="/books.html">
                        도서 둘러보기
                    </a>

                </div>

            `;

            return;

        }


        container.innerHTML =
            books
                .slice(0, 6)
                .map(
                    createMiniBookCard
                )
                .join("");


    } catch (error) {

        console.error(error);


        container.innerHTML = `

            <p class="bk-error-text">
                도서 정보를 불러오지 못했습니다.
            </p>

        `;

    }

}


/* =====================================================
   MINI BOOK CARD
===================================================== */

function createMiniBookCard(book) {

    const id =
        book.id;


    const title =
        escapeHtml(
            book.title || "도서"
        );


    const author =
        escapeHtml(
            book.author || ""
        );


    const thumbnail =
        escapeHtml(
            book.thumbnail || ""
        );


    const price =
        Number(
            book.salePrice ??
            book.price ??
            0
        );


    return `

        <article
            class="bk-mini-book"
            onclick="goBook(${id})"
        >

            <img
                src="${thumbnail}"
                alt="${title}"
                onerror="this.style.visibility='hidden'"
            >


            <div>

                <h4>
                    ${title}
                </h4>

                <p>
                    ${author}
                </p>

                <strong>
                    ${price.toLocaleString()}원
                </strong>

            </div>

        </article>

    `;

}


/* =====================================================
   MY REVIEWS
===================================================== */

async function loadMyReviews() {

    const container =
        document.getElementById(
            "myReviews"
        );


    if (!container) {
        return;
    }


    try {

        const response =
            await fetch(
                "/members/me/reviews",
                {
                    headers:
                        authHeaders()
                }
            );


        if (!response.ok) {

            throw new Error(
                "리뷰 조회 실패"
            );

        }


        const reviews =
            await response.json();


        if (
            !Array.isArray(reviews) ||
            reviews.length === 0
        ) {

            container.innerHTML = `

                <div class="bk-empty-state compact">

                    <p>
                        작성한 리뷰가 없습니다.
                    </p>

                </div>

            `;

            return;

        }


        container.innerHTML =
            reviews
                .map(
                    createMyReviewHtml
                )
                .join("");


    } catch (error) {

        console.error(error);


        container.innerHTML = `

            <p class="bk-error-text">
                리뷰를 불러오지 못했습니다.
            </p>

        `;

    }

}


/* =====================================================
   REVIEW CARD
===================================================== */

function createMyReviewHtml(review) {

    const title =
        escapeHtml(
            review.bookTitle || "도서"
        );


    const content =
        escapeHtml(
            review.content || ""
        );


    const rating =
        Number(
            review.rating || 0
        );


    return `

        <article
            class="bk-review-item"
            id="myReview-${review.id}"
        >

            <div
                id="reviewView-${review.id}"
                class="bk-review-view"
            >

                <div class="bk-review-item-head">

                    <strong>
                        ${title}
                    </strong>


                    <div class="bk-review-actions">

                        <span class="bk-stars">
                            ${"★".repeat(rating)}
                            ${"☆".repeat(5 - rating)}
                        </span>


                        <button
                            type="button"
                            class="bk-review-edit-button"
                            onclick="openReviewEdit(
                                ${review.id},
                                ${rating},
                                '${escapeJsString(
        review.content || ""
    )}'
                            )"
                        >
                            수정
                        </button>


                        <button
                            type="button"
                            class="bk-danger-link"
                            onclick="deleteMyReview(${review.id})"
                        >
                            삭제
                        </button>

                    </div>

                </div>


                <p>
                    ${content}
                </p>


                <time>
                    ${formatDate(
        review.createdAt
    )}
                </time>

            </div>


            <div
                id="reviewEdit-${review.id}"
                class="bk-review-edit"
                hidden
            >

                <div class="bk-review-edit-header">

                    <strong>
                        ${title}
                    </strong>

                    <span>
                        리뷰 수정
                    </span>

                </div>


                <label>
                    별점
                </label>

                <select
                    id="reviewRating-${review.id}"
                    class="bk-review-edit-rating"
                >

                    <option value="5">
                        ★★★★★
                    </option>

                    <option value="4">
                        ★★★★
                    </option>

                    <option value="3">
                        ★★★
                    </option>

                    <option value="2">
                        ★★
                    </option>

                    <option value="1">
                        ★
                    </option>

                </select>


                <label>
                    리뷰 내용
                </label>

                <textarea
                    id="reviewContent-${review.id}"
                    class="bk-review-edit-content"
                ></textarea>


                <div class="bk-review-edit-actions">

                    <button
                        type="button"
                        class="bk-secondary"
                        onclick="cancelReviewEdit(${review.id})"
                    >
                        취소
                    </button>


                    <button
                        type="button"
                        class="bk-primary"
                        id="reviewSaveButton-${review.id}"
                        onclick="updateMyReview(${review.id})"
                    >
                        수정 완료
                    </button>

                </div>

            </div>

        </article>

    `;

}


/* =====================================================
   REVIEW EDIT
===================================================== */

function openReviewEdit(
    reviewId,
    rating,
    content
) {

    const view =
        document.getElementById(
            "reviewView-" + reviewId
        );


    const edit =
        document.getElementById(
            "reviewEdit-" + reviewId
        );


    const ratingSelect =
        document.getElementById(
            "reviewRating-" + reviewId
        );


    const contentInput =
        document.getElementById(
            "reviewContent-" + reviewId
        );


    if (
        !view ||
        !edit ||
        !ratingSelect ||
        !contentInput
    ) {
        return;
    }


    view.hidden = true;
    edit.hidden = false;


    ratingSelect.value =
        String(rating);


    contentInput.value =
        content;


    contentInput.focus();

}


/* =====================================================
   REVIEW EDIT CANCEL
===================================================== */

function cancelReviewEdit(
    reviewId
) {

    const view =
        document.getElementById(
            "reviewView-" + reviewId
        );


    const edit =
        document.getElementById(
            "reviewEdit-" + reviewId
        );


    if (
        !view ||
        !edit
    ) {
        return;
    }


    edit.hidden = true;
    view.hidden = false;

}


/* =====================================================
   REVIEW UPDATE
===================================================== */

async function updateMyReview(
    reviewId
) {

    const ratingElement =
        document.getElementById(
            "reviewRating-" +
            reviewId
        );


    const contentElement =
        document.getElementById(
            "reviewContent-" +
            reviewId
        );


    const button =
        document.getElementById(
            "reviewSaveButton-" +
            reviewId
        );


    if (
        !ratingElement ||
        !contentElement ||
        !button
    ) {
        return;
    }


    const rating =
        Number(
            ratingElement.value
        );


    const content =
        contentElement
            .value
            .trim();


    if (!content) {

        alert(
            "리뷰 내용을 입력해주세요."
        );

        return;

    }


    button.disabled = true;
    button.textContent =
        "수정 중...";


    try {

        const response =
            await fetch(
                "/reviews/" + reviewId,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json",

                        ...authHeaders()
                    },

                    body:
                        JSON.stringify({
                            rating,
                            content
                        })
                }
            );


        const data =
            await response
                .json()
                .catch(
                    () => ({})
                );


        if (!response.ok) {

            throw new Error(
                data.message ||
                "리뷰 수정에 실패했습니다."
            );

        }


        alert(
            "리뷰가 수정되었습니다."
        );


        await loadMyReviews();


    } catch (error) {

        alert(
            error.message ||
            "리뷰 수정 중 오류가 발생했습니다."
        );


        button.disabled = false;
        button.textContent =
            "수정 완료";

    }

}


/* =====================================================
   REVIEW DELETE
===================================================== */

async function deleteMyReview(
    reviewId
) {

    if (
        !confirm(
            "이 리뷰를 삭제하시겠습니까?"
        )
    ) {
        return;
    }


    try {

        const response =
            await fetch(
                "/reviews/" + reviewId,
                {
                    method: "DELETE",

                    headers:
                        authHeaders()
                }
            );


        if (!response.ok) {

            throw new Error(
                "리뷰 삭제에 실패했습니다."
            );

        }


        alert(
            "리뷰가 삭제되었습니다."
        );


        await loadMyReviews();


    } catch (error) {

        alert(
            error.message
        );

    }

}


/* =====================================================
   LOGOUT
===================================================== */

function logout() {

    localStorage.removeItem(
        "token"
    );


    location.href =
        "/login.html";

}


/* =====================================================
   DELETE MEMBER
===================================================== */

async function deleteMember() {

    const confirmed =
        confirm(
            "정말 탈퇴하시겠습니까?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                "/members/me",
                {
                    method:
                        "DELETE",

                    headers:
                        authHeaders()
                }
            );


        if (!response.ok) {

            throw new Error(
                "회원 탈퇴에 실패했습니다."
            );

        }


        localStorage.removeItem(
            "token"
        );


        alert(
            "회원 탈퇴가 완료되었습니다."
        );


        location.href =
            "/mainpage.html";


    } catch (error) {

        alert(
            error.message
        );

    }

}


/* =====================================================
   NAVIGATION
===================================================== */

function goUpdate() {

    location.href =
        "/member-update.html";

}


function goBook(id) {

    if (
        id === null ||
        id === undefined
    ) {
        return;
    }


    location.href =
        "/book-detail.html?id=" +
        id;

}


/* =====================================================
   UTIL
===================================================== */

function setText(
    id,
    value
) {

    const element =
        document.getElementById(
            id
        );


    if (element) {

        element.textContent =
            value;

    }

}


function formatDate(value) {

    if (!value) {
        return "";
    }


    const date =
        new Date(value);


    return Number.isNaN(
        date.getTime()
    )
        ? value
        : date.toLocaleDateString(
            "ko-KR"
        );

}


function escapeHtml(value) {

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


/* =====================================================
   JS STRING ESCAPE
===================================================== */

function escapeJsString(
    value
) {

    return String(
        value ?? ""
    )
        .replaceAll(
            "\\",
            "\\\\"
        )
        .replaceAll(
            "'",
            "\\'"
        )
        .replaceAll(
            "\n",
            "\\n"
        )
        .replaceAll(
            "\r",
            ""
        );

}