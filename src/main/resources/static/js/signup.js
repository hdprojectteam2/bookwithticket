let emailChecked = false;



// 이메일 중복 확인

function checkEmail(){


    const email =
        document.getElementById("email").value;



    if(email === ""){

        alert("이메일을 입력해주세요.");

        return;

    }



    fetch(
        "/members/check-email?email=" + email
    )


        .then(response => response.json())


        .then(data => {



            const result =
                document.getElementById("emailCheck");



            if(data.available){


                result.innerText =
                    "사용 가능한 이메일입니다.";


                result.style.color =
                    "green";


                emailChecked = true;


            } else {


                result.innerText =
                    "이미 가입된 이메일입니다.";


                result.style.color =
                    "red";


                emailChecked = false;

            }


        })


        .catch(error => {

            console.log(error);

            alert("이메일 확인 중 오류 발생");

        });


}






// 회원가입

function signup(){



    const email =
        document.getElementById("email").value;



    const password =
        document.getElementById("password").value;



    const passwordCheck =
        document.getElementById("passwordCheck").value;



    const name =
        document.getElementById("name").value;



    const phone =
        document.getElementById("phone").value;



    const zipcode =
        document.getElementById("zipcode").value;



    const address =
        document.getElementById("address").value;



    const detailAddress =
        document.getElementById("detailAddress").value;



    const agree =
        document.getElementById("agree").checked;



    const marketingAgree =
        document.getElementById("marketingAgree").checked;





    // 검사



    if(!emailChecked){

        alert(
            "이메일 중복 확인을 해주세요."
        );

        return;

    }




    if(password !== passwordCheck){


        alert(
            "비밀번호가 일치하지 않습니다."
        );


        return;

    }





    if(!agree){


        alert(
            "필수 약관에 동의해주세요."
        );


        return;

    }





    const data = {


        email: email,


        password: password,


        name: name,


        phone: phone,


        zipcode: zipcode,


        address: address,


        detailAddress: detailAddress,


        marketingAgree: marketingAgree


    };






    fetch("/members/signup", {



        method:"POST",



        headers:{


            "Content-Type":
                "application/json"


        },



        body: JSON.stringify(data)



    })



        .then(response => {



            if(!response.ok){


                throw new Error(
                    "회원가입 실패"
                );


            }



            return response.json();


        })



        .then(result => {



            alert(
                "회원가입 성공!"
            );



            location.href =
                "/login.html";



        })



        .catch(error => {



            alert(
                error.message
            );



        });



}