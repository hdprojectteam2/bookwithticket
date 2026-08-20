/* =====================================================
   INIT
===================================================== */

document.addEventListener(
    "DOMContentLoaded",
    () => {

        getMemberInfo();


        const updateButton =
            document.getElementById(
                "memberUpdateButton"
            );


        const toggleButton =
            document.getElementById(
                "updatePasswordToggle"
            );


        updateButton?.addEventListener(
            "click",
            updateMember
        );


        toggleButton?.addEventListener(
            "click",
            () => {

                togglePassword(
                    "password",
                    toggleButton
                );

            }
        );

    }
);


/* =====================================================
   TOKEN
===================================================== */

function getToken() {

    return localStorage.getItem(
        "token"
    );

}


/* =====================================================
   MEMBER INFO
===================================================== */

async function getMemberInfo() {

    if (!getToken()) {

        location.href =
            "/login.html";

        return;

    }


    try {

        const response =
            await fetch(
                "/members/me",
                {
                    headers: {
                        Authorization:
                            "Bearer " +
                            getToken()
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                "회원정보를 불러오지 못했습니다."
            );

        }


        const member =
            await response.json();


        const emailElement =
            document.getElementById(
                "email"
            );


        if (emailElement) {

            emailElement.textContent =
                member.email || "-";

        }


        setValue(
            "name",
            member.name
        );

        setValue(
            "phone",
            member.phone
        );

        setValue(
            "zipcode",
            member.zipcode
        );

        setValue(
            "address",
            member.address
        );

        setValue(
            "detailAddress",
            member.detailAddress
        );


        const marketingAgree =
            document.getElementById(
                "marketingAgree"
            );


        if (marketingAgree) {

            marketingAgree.checked =
                Boolean(
                    member.marketingAgree
                );

        }


    } catch (error) {

        setMessage(
            "memberUpdateMessage",
            error.message,
            "error"
        );

    }

}


/* =====================================================
   UPDATE MEMBER
===================================================== */

async function updateMember() {

    const button =
        document.getElementById(
            "memberUpdateButton"
        );


    const passwordInput =
        document.getElementById(
            "password"
        );


    const marketingAgree =
        document.getElementById(
            "marketingAgree"
        );


    const data = {

        name:
            getValue("name") ||
            null,

        password:
            passwordInput?.value ||
            null,

        phone:
            getValue("phone"),

        zipcode:
            getValue("zipcode"),

        address:
            getValue("address"),

        detailAddress:
            getValue(
                "detailAddress"
            ),

        marketingAgree:
            marketingAgree?.checked ||
            false

    };


    /* 이름 필수 */

    if (!data.name) {

        setMessage(
            "memberUpdateMessage",
            "이름을 입력해주세요.",
            "error"
        );

        return;

    }


    /* 비밀번호 입력 시 길이 검사 */

    if (
        data.password &&
        (
            data.password.length < 8 ||
            data.password.length > 64
        )
    ) {

        setMessage(
            "memberUpdateMessage",
            "비밀번호는 8~64자로 입력해주세요.",
            "error"
        );

        return;

    }


    if (button) {

        button.disabled = true;
        button.textContent =
            "저장 중...";

    }


    try {

        const response =
            await fetch(
                "/members/me",
                {
                    method: "PUT",

                    headers: {

                        "Content-Type":
                            "application/json",

                        Authorization:
                            "Bearer " +
                            getToken()

                    },

                    body:
                        JSON.stringify(
                            data
                        )

                }
            );


        const result =
            await response
                .json()
                .catch(
                    () => ({})
                );


        if (!response.ok) {

            throw new Error(
                result.message ||
                "회원정보 수정에 실패했습니다."
            );

        }


        setMessage(
            "memberUpdateMessage",
            "회원정보가 수정되었습니다.",
            "success"
        );


        setTimeout(
            () => {

                location.href =
                    "/mypage.html";

            },
            600
        );


    } catch (error) {

        setMessage(
            "memberUpdateMessage",
            error.message,
            "error"
        );


        if (button) {

            button.disabled = false;

            button.textContent =
                "변경사항 저장";

        }

    }

}


/* =====================================================
   VALUE UTIL
===================================================== */

function getValue(id) {

    const element =
        document.getElementById(
            id
        );


    return element?.value
        .trim() || "";

}


function setValue(
    id,
    value
) {

    const element =
        document.getElementById(
            id
        );


    if (element) {

        element.value =
            value ?? "";

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
        input.type === "password";


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
   MESSAGE
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
        "bk-message " +
        type;

}