window.onload = function(){

    getMyInfo();

};





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






function logout(){


    localStorage.removeItem("token");


    alert(
        "로그아웃 되었습니다."
    );


    location.href="/login.html";


}







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


            localStorage.removeItem("token");


            location.href="/login.html";


        })



        .catch(error=>{


            alert(error.message);


        });


}






function goUpdate(){


    location.href="/member-update.html";


}