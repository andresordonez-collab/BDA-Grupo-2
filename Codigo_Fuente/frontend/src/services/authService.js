import http from '../http-common';

const aBase64 = (bytes) => btoa(String.fromCharCode(...new Uint8Array(bytes)));

const desdeBase64 = (texto) => Uint8Array.from(atob(texto), (c) => c.charCodeAt(0));

// Tarea 2: Cifra la contraseña con la clave pública del servidor (RSA-OAEP) antes de enviarla.
// Formato cifrado: "nonce|marca de tiempo|contraseña". Devuelve null si el navegador no admite WebCrypto.
const cifrarPassword = async (password) => {
  if (!window.crypto?.subtle) return null;

  try {
    const { data } = await http.get('/api/auth/clave-publica');
    const clave = await window.crypto.subtle.importKey(
      'spki',
      desdeBase64(data.clave),
      { name: 'RSA-OAEP', hash: 'SHA-256' },
      false,
      ['encrypt'],
    );

    const nonce = Array.from(window.crypto.getRandomValues(new Uint8Array(16)))
      .map((byte) => byte.toString(16).padStart(2, '0'))
      .join('');
    const texto = new TextEncoder().encode(`${nonce}|${Date.now()}|${password}`);
    const cifrado = await window.crypto.subtle.encrypt({ name: 'RSA-OAEP' }, clave, texto);

    return aBase64(cifrado);
  } catch {
    return null;
  }
};

const login = async (email, password) => {
  const passwordCifrada = await cifrarPassword(password);
  const cuerpo = passwordCifrada ? { email, passwordCifrada } : { email, password };

  const { data } = await http.post('/api/auth/login', cuerpo);

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
