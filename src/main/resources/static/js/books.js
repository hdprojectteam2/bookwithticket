/* =====================================================
   STATE
===================================================== */

let currentKeyword = "";
let currentCategory = "";
let currentSort = "latest";
let currentPage = 0;

const PAGE_SIZE = 12;


/* =====================================================
   INIT
===================================================== */

document.addEventListener("DOMContentLoaded", () => {

    bindBookEvents();

    loadCategories();
    loadPopular();
    loadNewBooks();
    loadAllBooks();

});


/* =====================================================
   EVENTS
===================================================== */

function bindBookEvents() {

    const searchInput =
        document.getElementById("searchInput");

    const searchButton =
        document.getElementById("searchButton");

    const sortSelect =
        document.getElementById("sortSelect");

    const resetButton =
        document.getElementById("resetBooksButton");


    searchButton?.addEventListener(
        "click",
        searchBook
    );


    sortSelect?.addEventListener(
        "change",
        event => {

            currentSort =
                event.target.value;

            currentPage = 0;

            loadAllBooks();

        }
    );


    resetButton?.addEventListener(
        "click",
        resetBooks
    );


    searchInput?.addEventListener(
        "keydown",
        event => {

            if (event.key === "Enter") {
                searchBook();
            }

        }
    );


    let timer;


    searchInput?.addEventListener(
        "input",
        event => {

            clearTimeout(timer);

            const keyword =
                event.target.value.trim();


            if (!keyword) {

                clearSuggestions();

                return;

            }


            timer = setTimeout(
                () => loadAutocomplete(keyword),
                220
            );

        }
    );


    document.addEventListener(
        "click",
        event => {

            if (
                !event.target.closest(
                    ".bk-search-area"
                )
            ) {

                clearSuggestions();

            }

        }
    );

}


/* =====================================================
   CATEGORY
===================================================== */

async function loadCategories() {

    const categoryBox =
        document.getElementById(
            "categoryBox"
        );


    try {

        const response =
            await fetch(
                "/books/categories"
            );


        if (!response.ok) {

            throw new Error(
                "카테고리 조회 실패"
            );

        }


        const categories =
            await response.json();


        categories.forEach(
            category => {

                const button =
                    document.createElement(
                        "button"
                    );


                button.type =
                    "button";

                button.dataset.category =
                    category.code;

                button.textContent =
                    category.name;


                button.addEventListener(
                    "click",
                    () => {

                        selectCategory(
                            button
                        );

                    }
                );


                categoryBox.appendChild(
                    button
                );

            }
        );


        const allButton =
            categoryBox.querySelector(
                '[data-category=""]'
            );


        allButton?.addEventListener(
            "click",
            event => {

                selectCategory(
                    event.currentTarget
                );

            }
        );


    } catch (error) {

        console.warn(
            "카테고리 로드 실패",
            error
        );

    }

}


function selectCategory(button) {

    document
        .querySelectorAll(
            "#categoryBox button"
        )
        .forEach(item => {

            item.classList.remove(
                "active"
            );

        });


    button.classList.add(
        "active"
    );


    currentCategory =
        button.dataset.category || "";

    currentPage = 0;


    loadAllBooks();

}


/* =====================================================
   POPULAR BOOKS
===================================================== */

async function loadPopular() {

    await renderCollection(
        "/books/popular",
        "popularBooks",
        5
    );

}


/* =====================================================
   NEW BOOKS
===================================================== */

async function loadNewBooks() {

    await renderCollection(
        "/books/new",
        "newBooks",
        5
    );

}


/* =====================================================
   POPULAR / NEW COMMON
===================================================== */

async function renderCollection(
    url,
    targetId,
    limit
) {

    const container =
        document.getElementById(
            targetId
        );


    try {

        const response =
            await fetch(url);


        if (!response.ok) {

            throw new Error(
                "도서 조회 실패"
            );

        }


        const books =
            await response.json();


        if (
            !Array.isArray(books) ||
            books.length === 0
        ) {

            container.innerHTML = `
                <p class="bk-empty-text">
                    표시할 도서가 없습니다.
                </p>
            `;

            return;

        }


        container.innerHTML =
            books
                .slice(0, limit)
                .map(createBookCard)
                .join("");


    } catch (error) {

        console.error(error);


        container.innerHTML = `
            <p class="bk-error-text">
                도서를 불러오지 못했습니다.
            </p>
        `;

    }

}


/* =====================================================
   ALL BOOKS
===================================================== */

async function loadAllBooks() {

    const container =
        document.getElementById(
            "allBooks"
        );

    const empty =
        document.getElementById(
            "booksEmpty"
        );


    container.innerHTML =
        createSkeletons(8);

    empty.hidden = true;


    const params =
        new URLSearchParams({
            sort: currentSort,
            page: currentPage,
            size: PAGE_SIZE
        });


    if (currentKeyword) {

        params.set(
            "keyword",
            currentKeyword
        );

    }


    if (currentCategory) {

        params.set(
            "category",
            currentCategory
        );

    }


    try {

        const response =
            await fetch(
                "/books?" +
                params.toString()
            );


        if (!response.ok) {

            throw new Error(
                "전체 도서 조회 실패"
            );

        }


        const data =
            await response.json();


        const books =
            Array.isArray(data)
                ? data
                : data.content || [];


        const totalElements =
            Array.isArray(data)
                ? books.length
                : data.totalElements || 0;


        const totalPages =
            Array.isArray(data)
                ? 1
                : data.totalPages || 0;


        updateResultText(
            totalElements
        );


        container.innerHTML = "";


        if (books.length === 0) {

            empty.hidden = false;

            renderPagination(0);

            return;

        }


        container.innerHTML =
            books
                .map(createBookCard)
                .join("");


        renderPagination(
            totalPages
        );


    } catch (error) {

        console.error(error);


        container.innerHTML = `

            <div class="bk-empty-state">

                <strong>
                    도서를 불러오지 못했습니다.
                </strong>

                <p>
                    잠시 후 다시 시도해주세요.
                </p>

                <button
                    class="bk-secondary"
                    onclick="loadAllBooks()"
                >
                    다시 시도
                </button>

            </div>

        `;

    }

}


/* =====================================================
   RESULT TEXT
===================================================== */

function updateResultText(
    totalElements
) {

    const resultText =
        document.getElementById(
            "bookResultText"
        );


    if (!resultText) {
        return;
    }


    if (currentKeyword) {

        resultText.textContent =
            `'${currentKeyword}' 검색 결과 ` +
            `${totalElements.toLocaleString()}권`;

        return;

    }


    resultText.textContent =
        `총 ${totalElements.toLocaleString()}권`;

}


/* =====================================================
   BOOK CARD
===================================================== */

function createBookCard(book) {

    const id =
        book.id;


    const title =
        escapeHtml(
            book.title ||
            "제목 없음"
        );


    const author =
        escapeHtml(
            book.author ||
            "저자 정보 없음"
        );


    const thumbnail =
        escapeHtml(
            book.thumbnail || ""
        );


    const category =
        escapeHtml(
            book.category || "도서"
        );


    const salePrice =
        Number(
            book.salePrice ??
            book.price ??
            0
        );


    const price =
        Number(
            book.price ?? 0
        );


    const discountRate =
        Number(
            book.discountRate || 0
        );


    const stock =
        Number(
            book.stock || 0
        );


    return `

        <article
            class="bk-book-card"
            onclick="detail(${id})"
        >

            <div class="bk-book-cover">

                ${
        thumbnail
            ? `
                            <img
                                src="${thumbnail}"
                                alt="${title}"
                                loading="lazy"
                                onerror="this.remove();"
                            >
                        `
            : `
                            <div class="bk-cover-placeholder">
                                <span>B</span>
                            </div>
                        `
    }


                ${
        discountRate > 0
            ? `
                            <span class="bk-discount-badge">
                                ${discountRate}%
                            </span>
                        `
            : ""
    }


                ${
        stock <= 0
            ? `
                            <span class="bk-soldout-badge">
                                품절
                            </span>
                        `
            : ""
    }

            </div>


            <div class="bk-book-body">

                <span class="bk-book-category">
                    ${category}
                </span>


                <h3>
                    ${title}
                </h3>


                <p>
                    ${author}
                </p>


                <div class="bk-book-price">

                    ${
        discountRate > 0
            ? `
                                <del>
                                    ${price.toLocaleString()}원
                                </del>
                            `
            : ""
    }


                    <strong>
                        ${salePrice.toLocaleString()}원
                    </strong>

                </div>

            </div>

        </article>

    `;

}


/* =====================================================
   SEARCH
===================================================== */

async function searchBook() {

    const input =
        document.getElementById("searchInput");

    currentKeyword =
        input.value.trim();

    currentPage = 0;

    clearSuggestions();

    // 검색 결과 로딩
    await loadAllBooks();

    // 검색 결과 영역으로 이동
    document
        .querySelector(".bk-all-books")
        ?.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
}


/* =====================================================
   AUTOCOMPLETE
===================================================== */

async function loadAutocomplete(
    keyword
) {

    const container =
        document.getElementById(
            "suggestions"
        );


    try {

        const response =
            await fetch(
                "/books/autocomplete?keyword=" +
                encodeURIComponent(keyword)
            );


        if (!response.ok) {

            throw new Error(
                "자동완성 조회 실패"
            );

        }


        const titles =
            await response.json();


        container.innerHTML =
            titles
                .slice(0, 6)
                .map(title => {

                    const safeTitle =
                        escapeHtml(title);


                    return `

                        <button
                            type="button"
                            data-title="${safeTitle}"
                        >

                            ⌕

                            <strong>
                                ${safeTitle}
                            </strong>

                        </button>

                    `;

                })
                .join("");


        container
            .querySelectorAll(
                "button"
            )
            .forEach(button => {

                button.addEventListener(
                    "click",
                    () => {

                        selectSuggestion(
                            button.dataset.title
                        );

                    }
                );

            });


    } catch (error) {

        console.error(error);

        clearSuggestions();

    }

}


function selectSuggestion(title) {

    const input =
        document.getElementById(
            "searchInput"
        );


    input.value = title;


    currentKeyword = title;

    currentPage = 0;


    clearSuggestions();

    loadAllBooks();

}


function clearSuggestions() {

    const container =
        document.getElementById(
            "suggestions"
        );


    if (container) {

        container.innerHTML = "";

    }

}


/* =====================================================
   PAGINATION
===================================================== */

function renderPagination(
    totalPages
) {

    const container =
        document.getElementById(
            "pagination"
        );


    container.innerHTML = "";


    if (totalPages <= 1) {
        return;
    }


    let start =
        Math.max(
            0,
            Math.min(
                currentPage - 2,
                totalPages - 5
            )
        );


    const end =
        Math.min(
            totalPages,
            start + 5
        );


    container.appendChild(
        createPageButton(
            "‹",
            currentPage - 1,
            currentPage === 0
        )
    );


    for (
        let page = start;
        page < end;
        page++
    ) {

        const button =
            createPageButton(
                String(page + 1),
                page,
                false
            );


        if (
            page === currentPage
        ) {

            button.classList.add(
                "active"
            );

        }


        container.appendChild(
            button
        );

    }


    container.appendChild(
        createPageButton(
            "›",
            currentPage + 1,
            currentPage >=
            totalPages - 1
        )
    );

}


function createPageButton(
    label,
    page,
    disabled
) {

    const button =
        document.createElement(
            "button"
        );


    button.type =
        "button";

    button.textContent =
        label;

    button.disabled =
        disabled;


    if (!disabled) {

        button.addEventListener(
            "click",
            () => {

                changePage(page);

            }
        );

    }


    return button;

}


function changePage(page) {

    currentPage = page;


    loadAllBooks();


    document
        .querySelector(
            ".bk-all-books"
        )
        ?.scrollIntoView({

            behavior: "smooth",
            block: "start"

        });

}


/* =====================================================
   RESET
===================================================== */

function resetBooks() {

    currentKeyword = "";
    currentCategory = "";
    currentSort = "latest";
    currentPage = 0;


    document.getElementById(
        "searchInput"
    ).value = "";


    document.getElementById(
        "sortSelect"
    ).value = "latest";


    document
        .querySelectorAll(
            "#categoryBox button"
        )
        .forEach(button => {

            button.classList.toggle(
                "active",
                button.dataset.category === ""
            );

        });


    loadAllBooks();

}


/* =====================================================
   SKELETON
===================================================== */

function createSkeletons(count) {

    return Array
        .from(
            { length: count },
            () => `

                <div class="bk-skeleton">

                    <div></div>

                    <span></span>

                    <span></span>

                </div>

            `
        )
        .join("");

}


/* =====================================================
   DETAIL
===================================================== */

function detail(id) {

    location.href =
        "/book-detail.html?id=" +
        id;

}


/* =====================================================
   HTML ESCAPE
===================================================== */

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