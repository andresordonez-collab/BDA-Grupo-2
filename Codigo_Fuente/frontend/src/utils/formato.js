export function formatearFechaHora(valor) {
  if (!valor) return '—';

  const fecha = new Date(valor);

  if (Number.isNaN(fecha.getTime())) {
    return String(valor);
  }

  return fecha.toLocaleString('es-CL', {
    dateStyle: 'short',
    timeStyle: 'short',
  });
}