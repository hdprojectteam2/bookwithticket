const params =
    new URLSearchParams(
        location.search
    );

console.log("book-detail.js 실행됨");

const id =
    params.get("id");



let favoriteStatus = false;



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
                book.thumbnail ?? "";



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
                book.price.toLocaleString();



            document.getElementById(
                "category"
            ).innerText =
                book.category ?? "-";



            document.getElementById(
                "stock"
            ).innerText =
                book.stock;



            document.getElementById(
                "description"
            ).innerText =
                book.description ?? "";



        });


}









// 관심 도서 추가/삭제

function favorite(){


    const token =
        localStorage.getItem("token");



    if(!token){

        alert(
            "로그인이 필요합니다."
        );

        return;

    }





    if(!favoriteStatus){



        fetch(
            "/members/favorites/" + id,
            {


                method:"POST",


                headers:{


                    "Authorization":
                        "Bearer " + token


                }


            }

        )


            .then(response=>{


                if(!response.ok){

                    throw new Error(
                        "관심 도서 등록 실패"
                    );

                }


                return response.json();


            })



            .then(()=>{


                favoriteStatus = true;


                alert(
                    "❤️ 관심 도서에 추가되었습니다."
                );


            })



            .catch(error=>{


                alert(
                    error.message
                );


            });



    }


    else {



        fetch(
            "/members/favorites/" + id,
            {


                method:"DELETE",


                headers:{


                    "Authorization":
                        "Bearer " + token


                }


            }

        )


            .then(()=>{


                favoriteStatus=false;


                alert(
                    "관심 도서에서 삭제되었습니다."
                );


            });



    }


}







function cart(){


    alert(
        "장바구니 기능은 담당자 구현 예정"
    );


}