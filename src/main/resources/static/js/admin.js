window.onload = function(){

    loadBooks();

};




// 도서 목록

function loadBooks(){


    fetch("/books")


        .then(response =>
            response.json()
        )


        .then(books=>{


            const box =
                document.getElementById(
                    "bookList"
                );


            box.innerHTML="";



            books.forEach(book=>{


                box.innerHTML += `


                <div class="book-card">


                    <h3>
                        ${book.title}
                    </h3>


                    <p>
                        저자 :
                        ${book.author}
                    </p>


                    <p>
                        출판사 :
                        ${book.publisher}
                    </p>


                    <p>
                        가격 :
                        ${book.price.toLocaleString()}
                    </p>


                    <p>
                        재고 :
                        ${book.stock}
                    </p>



                    <button onclick="editBook(${book.id})">

                        수정

                    </button>



                    <button onclick="updateStock(${book.id})">

                        재고 수정

                    </button>



                    <button onclick="deleteBook(${book.id})">

                        삭제

                    </button>


                </div>


                <hr>


                `;


            });



        });



}









// 도서 등록

function saveBook(){


    const data = {


        isbn:
        document.getElementById("isbn").value,


        title:
        document.getElementById("title").value,


        author:
        document.getElementById("author").value,


        publisher:
        document.getElementById("publisher").value,


        price:
            Number(
                document.getElementById("price").value
            ),


        thumbnail:
        document.getElementById("thumbnail").value,


        category:
        document.getElementById("category").value,


        stock:
            Number(
                document.getElementById("stock").value
            ),


        description:
        document.getElementById("description").value


    };





    fetch(
        "/books",
        {


            method:"POST",


            headers:{


                "Content-Type":
                    "application/json"


            },


            body:
                JSON.stringify(data)


        }

    )


        .then(response =>
            response.json()
        )


        .then(()=>{


            alert(
                "도서 등록 완료"
            );


            loadBooks();


        });



}









// 도서 수정

function editBook(id){


    const title =
        prompt(
            "변경할 제목"
        );



    const author =
        prompt(
            "변경할 저자"
        );



    const publisher =
        prompt(
            "변경할 출판사"
        );



    const price =
        prompt(
            "변경할 가격"
        );



    if(!title){

        return;

    }





    const data = {


        title:title,


        author:author,


        publisher:publisher,


        price:Number(price)


    };





    fetch(
        "/books/" + id,
        {


            method:"PUT",


            headers:{


                "Content-Type":
                    "application/json"


            },


            body:
                JSON.stringify(data)


        }

    )


        .then(response =>
            response.json()
        )


        .then(()=>{


            alert(
                "수정 완료"
            );


            loadBooks();


        });



}









// 재고 수정

function updateStock(id){


    const stock =
        prompt(
            "변경할 재고"
        );



    if(stock === null){

        return;

    }





    fetch(
        "/books/" + id + "/stock",
        {


            method:"PUT",


            headers:{


                "Content-Type":
                    "application/json"


            },


            body:
                JSON.stringify({

                    stock:Number(stock)

                })


        }

    )


        .then(response =>
            response.json()
        )


        .then(()=>{


            alert(
                "재고 수정 완료"
            );


            loadBooks();


        });



}









// 도서 삭제

function deleteBook(id){


    if(!confirm(
        "정말 삭제하시겠습니까?"
    )){


        return;


    }





    fetch(
        "/books/" + id,
        {


            method:"DELETE"


        }

    )


        .then(()=>{


            alert(
                "삭제 완료"
            );


            loadBooks();


        });



}