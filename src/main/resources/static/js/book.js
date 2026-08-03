window.onload = function(){

    loadPopular();

    loadNewBooks();

    loadAllBooks();

};






// 인기 도서

function loadPopular(){


    fetch("/books/popular")


        .then(response=>response.json())


        .then(data=>{


            const box =
                document.getElementById(
                    "popularBooks"
                );


            box.innerHTML="";


            data.forEach(book=>{


                box.innerHTML += createCard(book);


            });


        });


}








// 신간 도서

function loadNewBooks(){


    fetch("/books/new")


        .then(response=>response.json())


        .then(data=>{


            const box =
                document.getElementById(
                    "newBooks"
                );


            box.innerHTML="";


            data.forEach(book=>{


                box.innerHTML += createCard(book);


            });


        });


}








// 전체 도서

function loadAllBooks(){


    fetch("/books")


        .then(response=>response.json())


        .then(data=>{


            const box =
                document.getElementById(
                    "allBooks"
                );


            box.innerHTML="";


            data.forEach(book=>{


                box.innerHTML += createCard(book);


            });


        });


}








// 도서 카드

function createCard(book){


    return `

        <div class="book-card"
             onclick="detail(${book.id})">


            <img src="${book.thumbnail}"
                 width="120">


            <h3>
                ${book.title}
            </h3>


            <p>
                ${book.author}
            </p>


            <p>
                ${book.price}원
            </p>


        </div>

    `;


}








// 검색

function searchBook(){


    const keyword =
        document.getElementById(
            "searchInput"
        ).value;



    fetch(
        "/books/search?keyword="
        + keyword
    )


        .then(response=>response.json())


        .then(data=>{


            const box =
                document.getElementById(
                    "allBooks"
                );


            box.innerHTML="";


            data.forEach(book=>{


                box.innerHTML += createCard(book);


            });


        });


}








// 자동완성

document
    .getElementById("searchInput")
    .addEventListener(
        "keyup",
        function(){


            const keyword =
                this.value;



            if(keyword.length === 0){


                document.getElementById(
                    "suggestions"
                ).innerHTML="";


                return;

            }





            fetch(
                "/books/autocomplete?keyword="
                + keyword
            )


                .then(response=>response.json())


                .then(data=>{


                    const box =
                        document.getElementById(
                            "suggestions"
                        );


                    box.innerHTML="";



                    data.forEach(title=>{


                        box.innerHTML += `

                <div onclick="selectKeyword('${title}')">

                    ${title}

                </div>

            `;


                    });



                });



        });








function selectKeyword(title){


    document.getElementById(
        "searchInput"
    ).value = title;



    document.getElementById(
        "suggestions"
    ).innerHTML="";


}








// 카테고리

function loadCategory(category){


    fetch(
        "/books/category/"
        + category
    )


        .then(response=>response.json())


        .then(data=>{


            const box =
                document.getElementById(
                    "allBooks"
                );


            box.innerHTML="";


            data.forEach(book=>{


                box.innerHTML += createCard(book);


            });


        });


}








function detail(id){


    location.href =
        "/book-detail.html?id="
        + id;


}