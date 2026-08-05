const params =
    new URLSearchParams(
        location.search
    );


console.log("book-detail.js 실행됨");


const id =
    params.get("id");



let favoriteStatus = false;





window.onload = function(){


    loadBook();


    loadReviews();


    loadReviewInfo();


};









function loadBook(){


    fetch(
        "/books/" + id
    )


        .then(response =>
            response.json()
        )


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









// 관심 도서 추가 / 삭제

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











// 리뷰 조회

function loadReviews(){


    fetch(
        "/books/" + id + "/reviews"
    )


        .then(response =>
            response.json()
        )


        .then(reviews=>{


            const box =
                document.getElementById(
                    "reviewList"
                );



            box.innerHTML="";





            if(reviews.length === 0){


                box.innerHTML =
                    "<p>작성된 리뷰가 없습니다.</p>";


                return;


            }






            reviews.forEach(review=>{


                box.innerHTML += `


                <div class="review-card">


                    <h4>
                        ${review.name}
                    </h4>



                    <p>
                        ${"⭐".repeat(review.rating)}
                    </p>



                    <p>
                        ${review.content}
                    </p>



                    <small>
                        ${review.createdAt}
                    </small>


                </div>


            `;


            });



        });


}









// 리뷰 등록

function saveReview(){



    const token =
        localStorage.getItem("token");



    if(!token){


        alert(
            "로그인이 필요합니다."
        );


        return;

    }






    const data = {


        content:
        document.getElementById(
            "reviewContent"
        ).value,



        rating:
            Number(
                document.getElementById(
                    "rating"
                ).value
            )


    };






    fetch(
        "/books/" + id + "/reviews",
        {


            method:"POST",


            headers:{


                "Content-Type":
                    "application/json",


                "Authorization":
                    "Bearer " + token


            },


            body:
                JSON.stringify(data)


        }

    )



        .then(response=>{


            if(!response.ok){


                throw new Error(
                    "리뷰 등록 실패"
                );

            }


            return response.json();


        })



        .then(()=>{


            alert(
                "리뷰 등록 완료"
            );



            document.getElementById(
                "reviewContent"
            ).value="";



            loadReviews();


            loadReviewInfo();



        })



        .catch(error=>{


            alert(
                error.message
            );


        });



}











// 평균 평점 조회

function loadReviewInfo(){



    fetch(
        "/books/" + id + "/reviews/info"
    )


        .then(response =>
            response.json()
        )


        .then(info=>{


            document.getElementById(
                "reviewInfo"
            ).innerText =


                "⭐ "
                + info.averageRating
                + " / 5  ("
                + info.reviewCount
                + "개 리뷰)";



        });



}