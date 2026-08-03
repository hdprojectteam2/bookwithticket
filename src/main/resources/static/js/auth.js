function login(){


    const email =
        document.getElementById("email").value;


    const password =
        document.getElementById("password").value;



    fetch("/members/login", {

        method:"POST",

        headers:{
            "Content-Type":"application/json"
        },


        body: JSON.stringify({

            email: email,
            password: password

        })

    })


        .then(response => {

            if(!response.ok){

                throw new Error(
                    "로그인 실패"
                );

            }


            return response.json();

        })


        .then(data => {


            localStorage.setItem(
                "token",
                data.token
            );


            alert("로그인 성공");


            location.href="/mypage.html";


        })


        .catch(error => {

            alert(error.message);

        });


}