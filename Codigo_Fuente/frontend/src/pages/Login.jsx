import { useNavigate } from 'react-router-dom';

function Login() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
      <h1>Ingreso al Sistema de Bicicletas y Scooters</h1>
      <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
        <button onClick={() => navigate('/usuario')} style={{ padding: '0.5rem 1rem', cursor: 'pointer' }}>
          Entrar como Usuario
        </button>
        <button onClick={() => navigate('/operador')} style={{ padding: '0.5rem 1rem', cursor: 'pointer' }}>
          Entrar como Operador
        </button>
      </div>
    </div>
  );
}

export default Login;