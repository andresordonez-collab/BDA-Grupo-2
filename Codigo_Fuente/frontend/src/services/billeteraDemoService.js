const billeterasDemo = new Map();

function obtenerBilletera(idUsuario) {
  if (!idUsuario) {
    throw new Error('No pudimos identificar al usuario.');
  }

  if (!billeterasDemo.has(idUsuario)) {
    billeterasDemo.set(idUsuario, {
      saldo: 10000,
      simulada: true,
    });
  }

  return billeterasDemo.get(idUsuario);
}

async function getMiBilletera(idUsuario) {
  return { ...obtenerBilletera(idUsuario) };
}

async function pagarDeudaSimulada(idUsuario) {
  const billetera = obtenerBilletera(idUsuario);
  const monto = Math.max(0, -billetera.saldo);

  if (monto === 0) {
    throw new Error('No tienes deudas pendientes en esta demostración.');
  }

  const actualizada = {
    ...billetera,
    saldo: billetera.saldo + monto,
  };

  billeterasDemo.set(idUsuario, actualizada);

  return {
    billetera: { ...actualizada },
    monto,
  };
}

export default { getMiBilletera, pagarDeudaSimulada };