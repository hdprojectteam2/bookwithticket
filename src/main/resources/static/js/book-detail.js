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

    const token =
        localStorage.getItem("token");

    const headers = {};

    if(token){
        headers["Authorization"] =
            "Bearer " + token;
    }

    fetch(
        "/books/" + id,
        {
            headers: headers
        }
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









// 관심 도서

function favorite(){


    const token =
        localStorage.getItem("token");

    if(!token){


        alert(
            "로그인이 필요합니다."
        );

        location.href = "/login.html";

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
                    "❤️ 관심 도서 추가"
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
                    "관심 도서 삭제"
                );


            });


    }


}









async function cart(){
	
	const token =
	        localStorage.getItem("token");

	    if(!token){


	        alert(
	            "로그인이 필요합니다."
	        );

	        location.href = "/login.html";

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

		                        "Authorization":
		                            "Bearer " + token
		                    },

		                    body: 
								"bookId=" + encodeURIComponent(id) + "&quantity=1"
		                }
		            );

		        const message = await response.text();

		        if (!response.ok) {

		            console.error("서버 오류:", message);

		            alert(message || "도서 장바구니 추가에 실패했습니다.");

		            return;
		        }
				
		        alert(message);
				
		    } catch (error) {
		        
				console.error(error);
		        
				alert("요청 중 오류가 발생했습니다.");
		    }

}











// 내 회원 id 가져오기

function getMyEmail(){


    const token =
        localStorage.getItem("token");


    if(!token){

        return null;

    }


    const payload =
        JSON.parse(
            atob(
                token.split(".")[1]
            )
        );


    return payload.sub;

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



            box.innerHTML = "";



            if(reviews.length === 0){


                box.innerHTML =
                    "<p>작성된 리뷰가 없습니다.</p>";


                return;


            }





            const myEmail =
                getMyEmail();






            reviews.forEach(review=>{



                let deleteButton = "";

                console.log(
                    "내 이메일 : ",
                    myEmail
                );


                console.log(
                    "리뷰 이메일 : ",
                    review.email
                );


                if(
                    myEmail != null &&
                    myEmail === review.email
                ){


                    deleteButton = `


                <button onclick="deleteReview(${review.id})">

                    삭제

                </button>


                `;


                }






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



                <br>


                ${deleteButton}


            </div>


            `;



            });



        });



}











// 리뷰 작성

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



        });



}











// 리뷰 삭제

function deleteReview(reviewId){



    const token =
        localStorage.getItem("token");



    if(!confirm(
        "리뷰를 삭제하시겠습니까?"
    )){

        return;

    }





    fetch(
        "/reviews/" + reviewId,
        {


            method:"DELETE",


            headers:{


                "Authorization":
                    "Bearer " + token


            }


        }

    )



        .then(response=>{


            if(!response.ok){


                throw new Error(
                    "삭제 실패"
                );


            }


            return response.text();


        })


        .then(()=>{


            alert(
                "리뷰 삭제 완료"
            );



            loadReviews();


            loadReviewInfo();


        });



}











// 평균 평점

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