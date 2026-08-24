const params = new URLSearchParams(location.search);
const id = params.get("id");

let favoriteStatus = false;


/* =====================================================
   INIT
===================================================== */

document.addEventListener("DOMContentLoaded", () => {

    if (!id) {
        location.href = "/books.html";
        return;
    }

    loadBook();
    loadReviews();
    loadReviewInfo();
    loadFavoriteStatus();

});


/* =====================================================
   TOKEN
===================================================== */

function getToken() {
    return localStorage.getItem("token");
}


/* =====================================================
   BOOK DETAIL
===================================================== */

async function loadBook() {

    try {

        const headers = {};

        if (getToken()) {
            headers.Authorization =
                "Bearer " + getToken();
        }


        const response =
            await fetch(
                "/books/" + id,
                { headers }
            );


        if (!response.ok) {
            throw new Error(
                "도서 정보를 불러오지 못했습니다."
            );
        }


        const book =
            await response.json();


        setText("title", book.title);
        setText("breadcrumbTitle", book.title);

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
            Number(book.price ?? 0);

        const salePrice =
            Number(
                book.salePrice ??
                book.price ??
                0
            );

        const discountRate =
            Number(
                book.discountRate || 0
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
                ? price.toLocaleString() + "원"
                : ""
        );


        const image =
            document.getElementById(
                "thumbnail"
            );


        if (image) {

            image.src =
                book.thumbnail || "";

            image.onerror = () => {

                image.style.display =
                    "none";

            };

        }


        const cartButton =
            document.getElementById(
                "cartButton"
            );


        if (
            cartButton &&
            Number(book.stock || 0) <= 0
        ) {

            cartButton.disabled = true;
            cartButton.textContent = "품절";

        }

        // 연관 공연 조회 연동
        loadLinkedPerformance(book.id, book.title);

    } catch (error) {

        setText(
            "title",
            "도서 정보를 불러오지 못했습니다."
        );

        console.error(error);

    }

}

/* =====================================================
   LINKED PERFORMANCE (연관 공연 연동)
===================================================== */

async function loadLinkedPerformance(bookId, bookTitle) {
    try {
        const res = await fetch('/api/performances');
        if (!res.ok) return;
        const result = await res.json();
        const performances = (result && Array.isArray(result.data)) ? result.data : [];

        // 1. 도서 ID와 직접 연결된 공연 또는 2. 제목 키워드가 매칭되는 공연 탐색
        const linkedPerf = performances.find(p => 
            (p.originalBookId && p.originalBookId == bookId) || 
            (bookTitle && p.title && (
                (bookTitle.includes('오페라') && p.title.includes('오페라')) ||
                (bookTitle.includes('팬텀') && p.title.includes('팬텀')) ||
                (bookTitle.includes('레미제라블') && p.title.includes('레미제라블')) ||
                (bookTitle.includes('지킬') && p.title.includes('지킬'))
            ))
        );

        if (linkedPerf) {
            const box = document.getElementById('linkedPerformanceBox');
            const titleEl = document.getElementById('linkedPerfTitle');
            const venueEl = document.getElementById('linkedPerfVenue');
            const btnEl = document.getElementById('linkedPerfBtn');
            if (box && titleEl && venueEl && btnEl) {
                titleEl.textContent = linkedPerf.title;
                venueEl.textContent = `공연장: ${linkedPerf.venue} (${linkedPerf.runtimeMinutes || 150}분)`;
                btnEl.onclick = () => location.href = `/detail.html?id=${linkedPerf.id}`;
                box.style.display = 'block';
            }
        }
    } catch (e) {
        console.error('연관 공연 조회 실패:', e);
    }
}


/* =====================================================
   FAVORITE
===================================================== */

async function loadFavoriteStatus() {

    if (!getToken()) {
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


        if (!response.ok) {
            return;
        }


        const favorites =
            await response.json();


        favoriteStatus =
            favorites.some(
                book =>
                    String(book.id) ===
                    String(id)
            );


        updateFavoriteButton();


    } catch (error) {

        console.error(error);

    }

}


async function favorite() {

    if (!getToken()) {

        alert(
            "로그인이 필요합니다."
        );

        location.href =
            "/login.html";

        return;

    }


    try {

        const method =
            favoriteStatus
                ? "DELETE"
                : "POST";


        const response =
            await fetch(
                "/members/favorites/" + id,
                {
                    method,
                    headers: {
                        Authorization:
                            "Bearer " +
                            getToken()
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                "관심 도서 처리에 실패했습니다."
            );

        }


        favoriteStatus =
            !favoriteStatus;


        updateFavoriteButton();


    } catch (error) {

        alert(error.message);

    }

}


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

        alert(
            "로그인이 필요합니다."
        );

        location.href =
            "/login.html";

        return;

    }


    try {

        const response =
            await fetch(
                "/api/cart/items",
                {
                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/x-www-form-urlencoded",

                        Authorization:
                            "Bearer " +
                            getToken()

                    },

                    body:
                        "bookId=" +
                        encodeURIComponent(id) +
                        "&quantity=1"

                }
            );


			let result = null;

			try {
			    result = await response.json();
			} catch (error) {
			    result = null;
			}


        if (!response.ok) {

            throw new Error(
                result?.message ||
                "장바구니 추가에 실패했습니다."
            );

        }


        showToast("장바구니에 추가했니다.");


    } catch (error) {

        showToast(error.message);

    }

}

function showToast(message) {
    const toast = document.createElement("div");
    toast.className = "toast-message";
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add("show");
    }, 10);

    setTimeout(() => {

        toast.classList.remove("show");

        setTimeout(() => {
            toast.remove();
        }, 300);

    }, 2000);
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
            token.split(".")[1];


        const normalized =
            payload
                .replace(/-/g, "+")
                .replace(/_/g, "/");


        const decoded =
            JSON.parse(
                atob(normalized)
            );


        return decoded.sub || null;


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
            !Array.isArray(reviews) ||
            reviews.length === 0
        ) {

            container.innerHTML = `
                <div class="bk-empty-state compact">
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
                .map(review =>
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

        console.error(error);

    }

}


function createReviewHtml(
    review,
    myEmail
) {

    const rating =
        Number(review.rating || 0);


    const deleteButton =
        myEmail &&
        myEmail === review.email
            ? `
                <button
                    class="bk-danger-link"
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
                        ${escapeHtml(
        review.name ||
        "회원"
    )}
                    </strong>

                    <span class="bk-stars">

                        ${"★".repeat(rating)}
                        ${"☆".repeat(5 - rating)}

                    </span>

                </div>

                ${deleteButton}

            </div>


            <p>
                ${escapeHtml(
        review.content || ""
    )}
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
   SAVE REVIEW
===================================================== */

async function saveReview() {

    if (!getToken()) {

        alert(
            "로그인이 필요합니다."
        );

        location.href =
            "/login.html";

        return;

    }


    const content =
        document
            .getElementById(
                "reviewContent"
            )
            .value
            .trim();


    const rating =
        Number(
            document
                .getElementById(
                    "rating"
                )
                .value
        );


    if (!content) {

        alert(
            "리뷰 내용을 입력해주세요."
        );

        return;

    }


    const button =
        document.getElementById(
            "reviewSubmitButton"
        );


    if (button) {

        button.disabled = true;
        button.textContent =
            "등록 중...";

    }


    try {

        const response =
            await fetch(
                `/books/${id}/reviews`,
                {
                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json",

                        Authorization:
                            "Bearer " +
                            getToken()

                    },

                    body:
                        JSON.stringify({
                            content,
                            rating
                        })

                }
            );


        const data =
            await response
                .json()
                .catch(() => ({}));


        if (!response.ok) {

            throw new Error(
                data.message ||
                "리뷰 등록에 실패했습니다."
            );

        }


        document
            .getElementById(
                "reviewContent"
            )
            .value = "";


        await Promise.all([
            loadReviews(),
            loadReviewInfo()
        ]);


    } catch (error) {

        alert(error.message);


    } finally {

        if (button) {

            button.disabled = false;
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
                "/reviews/" + reviewId,
                {
                    method: "DELETE",

                    headers: {
                        Authorization:
                            "Bearer " +
                            getToken()
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                "리뷰 삭제에 실패했습니다."
            );

        }


        await Promise.all([
            loadReviews(),
            loadReviewInfo()
        ]);


    } catch (error) {

        alert(error.message);

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
            throw new Error();
        }


        const info =
            await response.json();


        const average =
            Number(
                info.averageRating || 0
            ).toFixed(1);


        const count =
            Number(
                info.reviewCount || 0
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

        console.error(error);

    }

}


/* =====================================================
   UTIL
===================================================== */

function setText(
    elementId,
    value
) {

    const element =
        document.getElementById(
            elementId
        );


    if (element) {

        element.textContent =
            value ?? "";

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