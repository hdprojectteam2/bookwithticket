let emailChecked = false;
let checkedEmail = "";


/* =====================================================
   INIT
===================================================== */

document.addEventListener(
    "DOMContentLoaded",
    () => {

        const emailInput =
            document.getElementById(
                "email"
            );

        const passwordInput =
            document.getElementById(
                "password"
            );

        const passwordCheckInput =
            document.getElementById(
                "passwordCheck"
            );

        const emailCheckButton =
            document.getElementById(
                "emailCheckButton"
            );

        const signupButton =
            document.getElementById(
                "signupButton"
            );


        emailCheckButton?.addEventListener(
            "click",
            checkEmail
        );


        signupButton?.addEventListener(
            "click",
            signup
        );


        document
            .querySelectorAll(
                ".signup-password-toggle"
            )
            .forEach(button => {

                button.addEventListener(
                    "click",
                    () => {

                        togglePassword(
                            button.dataset.target,
                            button
                        );

                    }
                );

            });


        /*
         * 이메일을 중복확인한 뒤
         * 다시 수정하면 확인 상태 초기화
         */
        emailInput?.addEventListener(
            "input",
            () => {

                if (
                    emailInput.value.trim()
                    !== checkedEmail
                ) {

                    emailChecked = false;


                    setFieldMessage(
                        "emailCheck",
                        "",
                        ""
                    );

                }

            }
        );


        /*
         * 비밀번호 확인
         */
        const checkPasswordMatch =
            () => {

                if (
                    !passwordCheckInput.value
                ) {

                    setFieldMessage(
                        "passwordCheckMessage",
                        "",
                        ""
                    );

                    return;

                }


                const matched =
                    passwordInput.value ===
                    passwordCheckInput.value;


                setFieldMessage(
                    "passwordCheckMessage",

                    matched
                        ? "비밀번호가 일치합니다."
                        : "비밀번호가 일치하지 않습니다.",

                    matched
                        ? "success"
                        : "error"
                );

            };


        passwordInput?.addEventListener(
            "input",
            checkPasswordMatch
        );


        passwordCheckInput?.addEventListener(
            "input",
            checkPasswordMatch
        );

    }
);


/* =====================================================
   EMAIL CHECK
===================================================== */

async function checkEmail() {

    const email =
        getValue("email");


    const button =
        document.getElementById(
            "emailCheckButton"
        );


    if (!email) {

        setFieldMessage(
            "emailCheck",
            "이메일을 입력해주세요.",
            "error"
        );

        return;

    }


    button.disabled = true;
    button.textContent =
        "확인 중...";


    try {

        const response =
            await fetch(
                "/members/check-email?email=" +
                encodeURIComponent(
                    email
                )
            );


        const data =
            await response
                .json()
                .catch(
                    () => ({})
                );


        if (!response.ok) {

            throw new Error(
                data.message ||
                "이메일 확인에 실패했습니다."
            );

        }


        emailChecked =
            Boolean(
                data.available
            );


        checkedEmail =
            emailChecked
                ? email
                : "";


        setFieldMessage(
            "emailCheck",

            emailChecked
                ? "사용 가능한 이메일입니다."
                : "이미 가입된 이메일입니다.",

            emailChecked
                ? "success"
                : "error"
        );


    } catch (error) {

        emailChecked = false;
        checkedEmail = "";


        setFieldMessage(
            "emailCheck",
            error.message ||
            "이메일 확인 중 오류가 발생했습니다.",
            "error"
        );


    } finally {

        button.disabled = false;

        button.textContent =
            "중복 확인";

    }

}


/* =====================================================
   SIGNUP
===================================================== */

async function signup() {

    const email =
        getValue("email");


    const password =
        document
            .getElementById(
                "password"
            )
            .value;


    const passwordCheck =
        document
            .getElementById(
                "passwordCheck"
            )
            .value;


    const name =
        getValue("name");

    const phone =
        getValue("phone");

    const zipcode =
        getValue("zipcode");

    const address =
        getValue("address");

    const detailAddress =
        getValue(
            "detailAddress"
        );


    const agree =
        document
            .getElementById(
                "agree"
            )
            .checked;


    const marketingAgree =
        document
            .getElementById(
                "marketingAgree"
            )
            .checked;


    const button =
        document.getElementById(
            "signupButton"
        );


    setMessage(
        "signupMessage",
        "",
        ""
    );


    /*
     * 이메일 중복 확인
     */
    if (
        !emailChecked ||
        checkedEmail !== email
    ) {

        setMessage(
            "signupMessage",
            "이메일 중복 확인을 해주세요.",
            "error"
        );

        return;

    }


    /*
     * 비밀번호 길이
     */
    if (
        password.length < 8 ||
        password.length > 64
    ) {

        setMessage(
            "signupMessage",
            "비밀번호는 8~64자로 입력해주세요.",
            "error"
        );

        return;

    }


    /*
     * 비밀번호 일치
     */
    if (
        password !==
        passwordCheck
    ) {

        setMessage(
            "signupMessage",
            "비밀번호가 일치하지 않습니다.",
            "error"
        );

        return;

    }


    /*
     * 이름 필수
     */
    if (!name) {

        setMessage(
            "signupMessage",
            "이름을 입력해주세요.",
            "error"
        );

        return;

    }


    /*
     * 필수 약관
     */
    if (!agree) {

        setMessage(
            "signupMessage",
            "필수 약관에 동의해주세요.",
            "error"
        );

        return;

    }


    button.disabled = true;
    button.textContent =
        "가입 중...";


    try {

        const response =
            await fetch(
                "/members/signup",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            {
                                email,
                                password,
                                name,
                                phone,
                                zipcode,
                                address,
                                detailAddress,
                                marketingAgree
                            }
                        )

                }
            );


        const data =
            await response
                .json()
                .catch(
                    () => ({})
                );


        if (!response.ok) {

            throw new Error(
                data.message ||
                "회원가입에 실패했습니다."
            );

        }


        alert(
            "회원가입이 완료되었습니다."
        );


        location.href =
            "/login.html";


    } catch (error) {

        setMessage(
            "signupMessage",
            error.message ||
            "회원가입 중 오류가 발생했습니다.",
            "error"
        );


    } finally {

        button.disabled = false;

        button.textContent =
            "가입하기";

    }

}


/* =====================================================
   PASSWORD TOGGLE
===================================================== */

function togglePassword(
    id,
    button
) {

    const input =
        document.getElementById(
            id
        );


    if (!input) {
        return;
    }


    const isHidden =
        input.type ===
        "password";


    input.type =
        isHidden
            ? "text"
            : "password";


    button.textContent =
        isHidden
            ? "숨기기"
            : "보기";

}


/* =====================================================
   VALUE
===================================================== */

function getValue(id) {

    return document
        .getElementById(id)
        ?.value
        .trim() || "";

}


/* =====================================================
   FIELD MESSAGE
===================================================== */

function setFieldMessage(
    id,
    text,
    type
) {

    const element =
        document.getElementById(
            id
        );


    if (!element) {
        return;
    }


    element.textContent =
        text;


    element.className =
        "bk-field-message" +
        (
            type
                ? " " + type
                : ""
        );

}


/* =====================================================
   FORM MESSAGE
===================================================== */

function setMessage(
    id,
    text,
    type
) {

    const element =
        document.getElementById(
            id
        );


    if (!element) {
        return;
    }


    element.textContent =
        text;


    element.className =
        "bk-message" +
        (
            type
                ? " " + type
                : ""
        );

}