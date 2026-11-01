import http from '../http-common';

const login = async (email, password) => {
  const { data } = await http.post('/api/auth/login', {
    email,
    password,
  });

  return data;
};

const logout = async () => {
  await http.post('/api/auth/logout');
};
const me = async () => {
  const { data } = await http.get('/api/auth/me');
  return data;
};

export default { login, logout, me };