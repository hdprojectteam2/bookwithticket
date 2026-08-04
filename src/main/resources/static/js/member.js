window.onload = function(){

    getMyInfo();

    loadRecentBooks();

    loadFavoriteBooks();

};







// 회원 정보 조회

function getMyInfo(){


    const token =
        localStorage.getItem("token");



    fetch("/members/me", {


        method:"GET",


        headers:{


            "Authorization":
                "Bearer " + token


        }


    })



        .then(response=>{


            if(!response.ok){

                throw new Error(
                    "로그인이 필요합니다."
                );

            }


            return response.json();


        })



        .then(member=>{


            document.getElementById(
                "memberName"
            ).innerText =
                member.name;



            document.getElementById(
                "email"
            ).innerText =
                member.email;



            document.getElementById(
                "name"
            ).innerText =
                member.name;



            document.getElementById(
                "phone"
            ).innerText =
                member.phone ?? "-";



            document.getElementById(
                "address"
            ).innerText =
                member.address ?? "-";


        })



        .catch(error=>{


            alert(error.message);


            location.href="/login.html";


        });


}









// 최근 본 도서 조회

function loadRecentBooks(){



    const token =
        localStorage.getItem("token");



    fetch("/members/recent-books", {


        method:"GET",


        headers:{


            "Authorization":
                "Bearer " + token


        }


    })



        .then(response=>{


            if(!response.ok){


                throw new Error(
                    "최근 본 도서를 불러올 수 없습니다."
                );


            }


            return response.json();


        })



        .then(books=>{


            const box =
                document.getElementById(
                    "recentBooks"
                );



            box.innerHTML = "";



            if(books.length === 0){


                box.innerHTML =

                    "<p>최근 본 도서가 없습니다.</p>";


                return;


            }






            books.forEach(book=>{


                box.innerHTML += `


                <div class="book-card">


                    <img src="${book.thumbnail ?? ''}"
                         width="100">


                    <h4>
                        ${book.title}
                    </h4>


                    <p>
                        ${book.author}
                    </p>


                </div>


            `;


            });



        })



        .catch(error=>{


            alert(error.message);


        });


}









// 로그아웃

function logout(){



    localStorage.removeItem(
        "token"
    );



    alert(
        "로그아웃 되었습니다."
    );



    location.href="/login.html";


}









// 회원 탈퇴

function deleteMember(){



    if(!confirm(
        "정말 탈퇴하시겠습니까?"
    )){


        return;


    }






    const token =
        localStorage.getItem("token");



    fetch("/members/me", {



        method:"DELETE",



        headers:{


            "Authorization":
                "Bearer " + token


        }


    })



        .then(response=>{


            if(!response.ok){


                throw new Error(
                    "탈퇴 실패"
                );


            }


            return response.json();


        })



        .then(()=>{


            alert(
                "회원 탈퇴 완료"
            );



            localStorage.removeItem(
                "token"
            );



            location.href="/login.html";


        })



        .catch(error=>{


            alert(error.message);


        });


}









// 회원정보 수정 이동

function goUpdate(){


    location.href =
        "/member-update.html";


}

function loadFavoriteBooks(){


    const token =
        localStorage.getItem("token");



    fetch(
        "/members/favorites",
        {


            method:"GET",


            headers:{


                "Authorization":
                    "Bearer " + token


            }


        }
    )


        .then(response=>{


            if(!response.ok){

                throw new Error(
                    "관심 도서를 불러올 수 없습니다."
                );

            }


            return response.json();


        })



        .then(books=>{


            const box =
                document.getElementById(
                    "favoriteBooks"
                );



            box.innerHTML="";



            if(books.length === 0){


                box.innerHTML =
                    "<p>관심 도서가 없습니다.</p>";


                return;


            }




            books.forEach(book=>{


                box.innerHTML += `


                <div class="book-card">


                    <img src="${book.thumbnail ?? ''}"
                         width="100">


                    <h4>
                        ${book.title}
                    </h4>


                    <p>
                        ${book.author}
                    </p>



                </div>


            `;


            });



        })



        .catch(error=>{


            alert(error.message);


        });


}