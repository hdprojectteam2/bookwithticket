const params =
    new URLSearchParams(
        location.search
    );



const id =
    params.get("id");





window.onload=function(){

    loadBook();

};







function loadBook(){


    fetch(
        "/books/" + id
    )


        .then(response=>response.json())


        .then(book=>{


            document.getElementById(
                "thumbnail"
            ).src =
                book.thumbnail;



            document.getElementById(
                "title"
            ).innerText =
                book.title;




            document.getElementById(
                "author"
            ).innerText =
                book.author;




            document.getElementById(
                "publisher"
            ).innerText =
                book.publisher;




            document.getElementById(
                "price"
            ).innerText =
                book.price;




            document.getElementById(
                "category"
            ).innerText =
                book.category;




            document.getElementById(
                "stock"
            ).innerText =
                book.stock;




            document.getElementById(
                "description"
            ).innerText =
                book.description;



        });


}








function favorite(){


    alert(
        "관심 도서 기능 연결 예정"
    );


}








function cart(){


    alert(
        "장바구니 기능 연결 예정"
    );


}