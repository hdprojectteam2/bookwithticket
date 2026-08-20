/* =====================================================
   INIT
===================================================== */

document.addEventListener(
    "DOMContentLoaded",
    () => {

        getMyInfo();
        loadRecentBooks();
        loadFavoriteBooks();
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

function createMyReviewHtml(
    review
) {

    const title =
        escapeHtml(
            review.bookTitle ||
            "도서"
        );


    const content =
        escapeHtml(
            review.content ||
            ""
        );


    const rating =
        Number(
            review.rating || 0
        );


    return `

        <article class="bk-review-item">

            <div class="bk-review-item-head">

                <strong>
                    ${title}
                </strong>

                <span class="bk-stars">

                    ${"★".repeat(rating)}
                    ${"☆".repeat(5 - rating)}

                </span>

            </div>


            <p>
                ${content}
            </p>


            <time>
                ${formatDate(
        review.createdAt
    )}
            </time>

        </article>

    `;

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