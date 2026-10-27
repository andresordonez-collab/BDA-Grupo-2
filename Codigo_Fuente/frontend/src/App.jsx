import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import PanelUsuario from './pages/PanelUsuario';
import PanelOperador from './pages/PanelOperador';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/usuario" element={<PanelUsuario />} />
        <Route path="/operador" element={<PanelOperador />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;