import { useEffect, useMemo, useState } from 'react';
import { Typography } from '@mui/material';
import EntityCrudPage from '../../components/common/EntityCrudPage';
import movimientoBilleteraService from '../../services/movimientoBilleteraService';
import billeteraService from '../../services/billeteraService';
import usuarioService from '../../services/usuarioService';
import { formatearFechaHora } from '../../utils/formato';

const formatoMonto = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const TIPOS = [
  { value: 'Recarga', label: 'Recarga' },
  { value: 'Cobro_viaje', label: 'Cobro de viaje' },
  { value: 'Ajuste', label: 'Ajuste' },
];

const ETIQUETA_TIPO = Object.fromEntries(
  TIPOS.map((tipo) => [tipo.value, tipo.label])
);

function fechaLocalActual() {
  const ahora = new Date();
  const dos = (numero) => String(numero).padStart(2, '0');

  return `${ahora.getFullYear()}-${dos(
    ahora.getMonth() + 1
  )}-${dos(ahora.getDate())}T${dos(ahora.getHours())}:${dos(
    ahora.getMinutes()
  )}`;
}

export default function MovimientosBilleteraPage() {
  const [billeteras, setBilleteras] = useState([]);
  const [usuarios, setUsuarios] = useState([]);

  useEffect(() => {
    let activo = true;

    Promise.allSettled([
      billeteraService.getAll(),
      usuarioService.getAll(),
    ]).then(([resBilleteras, resUsuarios]) => {
      if (!activo) return;

      if (resBilleteras.status === 'fulfilled') {
        const data = resBilleteras.value.data;
        setBilleteras(Array.isArray(data) ? data : []);
      }

      if (resUsuarios.status === 'fulfilled') {
        const data = resUsuarios.value.data;
        setUsuarios(Array.isArray(data) ? data : []);
      }
    });

    return () => {
      activo = false;
    };
  }, []);

  const mapaUsuarios = useMemo(
    () =>
      new Map(
        usuarios.map((usuario) => [
          String(usuario.idUsuario),
          usuario.nombreUsuario,
        ])
      ),
    [usuarios]
  );

  const opcionesBilletera = useMemo(
    () =>
      billeteras.map((billetera) => ({
        value: String(billetera.idBilletera),
        label: `Billetera #${billetera.idBilletera} · ${
          mapaUsuarios.get(String(billetera.idUsuario)) ??
          `Usuario #${billetera.idUsuario}`
        }`,
      })),
    [billeteras, mapaUsuarios]
  );

  const nombreBilletera = (item) => {
    const billetera = billeteras.find(
      (candidata) =>
        String(candidata.idBilletera) === String(item.idBilletera)
    );

    if (!billetera) {
      return `Billetera #${item.idBilletera}`;
    }

    return `Billetera #${billetera.idBilletera} · ${
      mapaUsuarios.get(String(billetera.idUsuario)) ??
      `Usuario #${billetera.idUsuario}`
    }`;
  };

  const columns = [
    {
      field: 'idMovimiento',
      label: 'ID',
      align: 'right',
    },
    {
      field: 'idBilletera',
      label: 'Billetera',
      render: nombreBilletera,
    },
    {
      field: 'idViaje',
      label: 'Viaje',
      align: 'right',
      render: (item) => item.idViaje ?? '—',
    },
    {
      field: 'tipoMovimiento',
      label: 'Tipo',
      render: (item) =>
        ETIQUETA_TIPO[item.tipoMovimiento] ??
        item.tipoMovimiento,
    },
    {
      field: 'monto',
      label: 'Monto',
      align: 'right',
      render: (item) => (
        <Typography variant="body2" sx={{ fontWeight: 700 }}>
          {formatoMonto.format(Number(item.monto ?? 0))}
        </Typography>
      ),
    },
    {
      field: 'fechaMovimiento',
      label: 'Fecha',
      render: (item) => formatearFechaHora(item.fechaMovimiento),
    },
  ];

  const fields = [
    {
      name: 'idBilletera',
      label: 'Billetera',
      type: 'select',
      options: opcionesBilletera,
      required: true,
    },
    {
      name: 'idViaje',
      label: 'ID de viaje',
      type: 'number',
      min: 1,
      step: 1,
      helperText: 'Opcional. Déjalo vacío si no está asociado a un viaje.',
    },
    {
      name: 'tipoMovimiento',
      label: 'Tipo de movimiento',
      type: 'select',
      options: TIPOS,
      required: true,
    },
    {
      name: 'monto',
      label: 'Monto (CLP)',
      type: 'number',
      min: 0,
      step: 0.01,
      required: true,
    },
    {
      name: 'fechaMovimiento',
      label: 'Fecha del movimiento',
      type: 'datetime-local',
      required: true,
    },
  ];

  const initialValues = {
    idBilletera: '',
    idViaje: '',
    tipoMovimiento: 'Recarga',
    monto: '',
    fechaMovimiento: fechaLocalActual(),
  };

  const toForm = (item) => ({
    idBilletera:
      item.idBilletera !== null && item.idBilletera !== undefined
        ? String(item.idBilletera)
        : '',
    idViaje:
      item.idViaje !== null && item.idViaje !== undefined
        ? String(item.idViaje)
        : '',
    tipoMovimiento: item.tipoMovimiento ?? 'Recarga',
    monto:
      item.monto !== null && item.monto !== undefined
        ? String(item.monto)
        : '',
    fechaMovimiento: item.fechaMovimiento
      ? String(item.fechaMovimiento).slice(0, 16)
      : fechaLocalActual(),
  });

  const toPayload = (form) => ({
    idBilletera: Number(form.idBilletera),
    idViaje: form.idViaje ? Number(form.idViaje) : null,
    tipoMovimiento: form.tipoMovimiento,
    monto: Number(form.monto),
    fechaMovimiento: form.fechaMovimiento
      ? `${form.fechaMovimiento}:00`
      : null,
  });

  const validate = (form) => {
    if (!form.idBilletera) {
      return 'Selecciona una billetera.';
    }

    if (!form.tipoMovimiento) {
      return 'Selecciona un tipo de movimiento.';
    }

    if (
      form.monto === '' ||
      Number.isNaN(Number(form.monto)) ||
      Number(form.monto) < 0
    ) {
      return 'El monto debe ser un número mayor o igual a 0.';
    }

    if (!form.fechaMovimiento) {
      return 'Selecciona la fecha del movimiento.';
    }

    if (form.idViaje) {
      const idViaje = Number(form.idViaje);

      if (!Number.isInteger(idViaje) || idViaje <= 0) {
        return 'El ID de viaje debe ser un entero mayor que cero.';
      }
    }

    return '';
  };

  return (
    <EntityCrudPage
      title="Movimientos de billetera"
      description="Registra recargas, cobros de viaje y ajustes de saldo."
      entityName="movimiento"
      createLabel="Nuevo movimiento"
      service={movimientoBilleteraService}
      idField="idMovimiento"
      columns={columns}
      fields={fields}
      initialValues={initialValues}
      toForm={toForm}
      toPayload={toPayload}
      validate={validate}
      getLabel={(item) =>
        `el movimiento #${item?.idMovimiento ?? ''}`
      }
    />
  );
}
