window.onload = function(){

    getMemberInfo();

};





function getMemberInfo(){


    const token =
        localStorage.getItem("token");



    fetch("/members/me", {


        method:"GET",


        headers:{


            "Authorization":
                "Bearer " + token


        }


    })


        .then(response=>response.json())


        .then(member=>{


            document.getElementById("email")
                .innerText = member.email;


            document.getElementById("name")
                .value = member.name ?? "";


            document.getElementById("phone")
                .value = member.phone ?? "";


            document.getElementById("zipcode")
                .value = member.zipcode ?? "";


            document.getElementById("address")
                .value = member.address ?? "";


            document.getElementById("detailAddress")
                .value = member.detailAddress ?? "";


        });


}







function updateMember(){


    const token =
        localStorage.getItem("token");



    const data = {


        name:
        document.getElementById("name").value,


        phone:
        document.getElementById("phone").value,


        zipcode:
        document.getElementById("zipcode").value,


        address:
        document.getElementById("address").value,


        detailAddress:
        document.getElementById("detailAddress").value,


        password:
            document.getElementById("password").value || null


    };




    fetch("/members/me", {


        method:"PUT",


        headers:{


            "Content-Type":
                "application/json",


            "Authorization":
                "Bearer " + token


        },


        body:
            JSON.stringify(data)


    })



        .then(response=>{


            if(!response.ok){

                throw new Error(
                    "수정 실패"
                );

            }


            return response.json();


        })



        .then(()=>{


            alert(
                "회원정보가 수정되었습니다."
            );


            location.href="/mypage.html";


        })



        .catch(error=>{


            alert(error.message);


        });


}