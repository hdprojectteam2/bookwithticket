document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    const toggle = document.getElementById('loginPasswordToggle');
    form?.addEventListener('submit', e => { e.preventDefault(); login(); });
    toggle?.addEventListener('click', () => togglePassword('password', toggle));
});

async function login() {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const button = document.getElementById('loginButton');
    setMessage('loginMessage', '', '');
    if (!email) return setMessage('loginMessage', '이메일을 입력해주세요.', 'error');
    if (!password) return setMessage('loginMessage', '비밀번호를 입력해주세요.', 'error');
    button.disabled = true; button.textContent = '로그인 중...';
    try {
        const response = await fetch('/members/login', {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email,password})});
        const data = await response.json().catch(()=>({}));
        if (!response.ok) throw new Error(data.message || '이메일 또는 비밀번호를 확인해주세요.');
        localStorage.setItem('token', data.accessToken);
        location.href = '/mypage.html';
    } catch (e) {
        setMessage('loginMessage', e.message || '로그인 중 오류가 발생했습니다.', 'error');
    } finally {
        button.disabled = false; button.textContent = '로그인';
    }
}

function togglePassword(id, button) {
    const input = document.getElementById(id); if (!input) return;
    const show = input.type === 'password'; input.type = show ? 'text' : 'password'; button.textContent = show ? '숨기기' : '보기';
}
function setMessage(id, text, type) { const el=document.getElementById(id); if(!el) return; el.textContent=text; el.className='bk-message'+(type?' '+type:''); }
