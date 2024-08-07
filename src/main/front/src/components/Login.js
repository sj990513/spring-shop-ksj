import React, { useState, useContext } from 'react';
import axiosInstance from '../axiosInstance';
import { useHistory } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './Login.css'; // CSS 파일을 import합니다.

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const history = useHistory();
  const { setIsLoggedIn, setUser } = useContext(AuthContext); // setUser를 가져옵니다.

  const handleLogin = async () => {
    try {
      const response = await axiosInstance.post('/login', { username, password });

      // Store the access token in local storage
      const accessToken = response.headers['access'];
      localStorage.setItem('access', accessToken);

      // Fetch user info
      const userInfoResponse = await axiosInstance.get('/check-auth'); // 로그인 후 사용자 정보를 받아옴

      // Update auth context
      setIsLoggedIn(true);
      setUser(userInfoResponse.data); // 사용자 정보를 AuthContext에 저장

      // Redirect to the main screen
      history.push('/');
    } catch (error) {
      console.error('There was an error logging in!', error);
      alert("로그인 실패. 아이디나 비밀번호를 확인해주세요.");
    }
  };

  return (
    <div className="login-container">
      <h2>로그인</h2>
      <form className="login-form" onSubmit={(e) => { e.preventDefault(); handleLogin(); }}>
        <div className="form-group">
          <label>아이디</label>
          <input
            type="text"
            placeholder="아이디"
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>비밀번호</label>
          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="login-btn">로그인</button>
      </form>
    </div>
  );
}

export default Login;