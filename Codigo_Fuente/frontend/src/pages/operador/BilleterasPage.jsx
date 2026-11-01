import { useEffect, useMemo, useState } from 'react';
import { Typography } from '@mui/material';
import EntityCrudPage from '../../components/common/EntityCrudPage';
import billeteraService from '../../services/billeteraService';
import usuarioService from '../../services/usuarioService';

const formatoSaldo = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export default function BilleterasPage() {
  const [usuarios, setUsuarios] = useState([]);

  useEffect(() => {
    let activo = true;

    usuarioService
      .getAll()
      .then(({ data }) => {
        if (activo) {
          setUsuarios(Array.isArray(data) ? data : []);
        }
      })
      .catch(() => {
        if (activo) {
          setUsuarios([]);
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

  const opcionesUsuario = useMemo(
    () =>
      usuarios.map((usuario) => ({
        value: String(usuario.idUsuario),
        label: `${usuario.nombreUsuario} · ${usuario.email}`,
      })),
    [usuarios]
  );

  const columns = [
    {
      field: 'idBilletera',
      label: 'ID',
      align: 'right',
    },
    {
      field: 'idUsuario',
      label: 'Usuario',
      render: (item) => {
        const usuario = usuarios.find(
          (candidato) =>
            String(candidato.idUsuario) === String(item.idUsuario)
        );

        return usuario
          ? `${usuario.nombreUsuario} · ${usuario.email}`
          : mapaUsuarios.get(String(item.idUsuario)) ??
              `#${item.idUsuario}`;
      },
    },
    {
      field: 'saldo',
      label: 'Saldo',
      align: 'right',
      render: (item) => (
        <Typography
          variant="body2"
          color={
            Number(item.saldo ?? 0) < 0
              ? 'error.main'
              : 'text.primary'
          }
          sx={{ fontWeight: 700 }}
        >
          {formatoSaldo.format(Number(item.saldo ?? 0))}
        </Typography>
      ),
    },
  ];

  const fields = [
    {
      name: 'idUsuario',
      label: 'Usuario',
      type: 'select',
      options: opcionesUsuario,
      required: true,
      helperText: 'Cada usuario puede tener una sola billetera.',
    },
    {
      name: 'saldo',
      label: 'Saldo (CLP)',
      type: 'number',
      step: 0.01,
      required: true,
      helperText:
        'Puede ser negativo si el usuario tiene deuda por viajes.',
    },
  ];

  const initialValues = {
    idUsuario: '',
    saldo: '0',
  };

  const toForm = (item) => ({
    idUsuario:
      item.idUsuario !== null && item.idUsuario !== undefined
        ? String(item.idUsuario)
        : '',
    saldo:
      item.saldo !== null && item.saldo !== undefined
        ? String(item.saldo)
        : '0',
  });

  const toPayload = (form) => ({
    idUsuario: Number(form.idUsuario),
    saldo: Number(form.saldo),
  });

  const validate = (form) => {
    if (!form.idUsuario) {
      return 'Selecciona un usuario.';
    }

    if (
      form.saldo === '' ||
      Number.isNaN(Number(form.saldo))
    ) {
      return 'El saldo debe ser un número.';
    }

    return '';
  };

  return (
    <EntityCrudPage
      title="Billeteras"
      description="Administra el saldo disponible de cada usuario."
      entityName="billetera"
      createLabel="Nueva billetera"
      service={billeteraService}
      idField="idBilletera"
      columns={columns}
      fields={fields}
      initialValues={initialValues}
      toForm={toForm}
      toPayload={toPayload}
      validate={validate}
      getLabel={(item) =>
        `la billetera #${item?.idBilletera ?? ''}`
      }
    />
  );
}
