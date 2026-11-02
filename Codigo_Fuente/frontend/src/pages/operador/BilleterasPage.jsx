import { useEffect, useMemo, useState } from 'react';
import { Typography } from '@mui/material';
import EntityCrudPage from '../../components/common/EntityCrudPage';
import billeteraService from '../../services/billeteraService';
import usuarioService from '../../services/usuarioService';
import rolService from '../../services/rolService';

const formatoSaldo = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export default function BilleterasPage() {
  const [usuarios, setUsuarios] = useState([]);
  const [roles, setRoles] = useState([]);
  const [billeteras, setBilleteras] = useState([]);
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    let activo = true;

    const lista = (resultado) =>
      resultado.status === 'fulfilled' && Array.isArray(resultado.value.data)
        ? resultado.value.data
        : [];

    Promise.allSettled([
      usuarioService.getAll(),
      rolService.getAll(),
      billeteraService.getAll(),
    ]).then(([resUsuarios, resRoles, resBilleteras]) => {
      if (!activo) return;

      setUsuarios(lista(resUsuarios));
      setRoles(lista(resRoles));
      setBilleteras(lista(resBilleteras));
    });

    return () => {
      activo = false;
    };
  }, [revision]);

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

  const opcionesUsuario = useMemo(() => {
    const nombreRol = new Map(
      roles.map((rol) => [String(rol.idRol), rol.nombreRol])
    );
    const conBilletera = new Set(
      billeteras.map((billetera) => String(billetera.idUsuario))
    );

    return usuarios
      .filter(
        (usuario) =>
          nombreRol.get(String(usuario.idRol)) === 'Usuario' &&
          !conBilletera.has(String(usuario.idUsuario))
      )
      .map((usuario) => ({
        value: String(usuario.idUsuario),
        label: `${usuario.nombreUsuario} · ${usuario.email}`,
      }));
  }, [usuarios, roles, billeteras]);

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
      showOnEdit: false,
      helperText:
        opcionesUsuario.length === 0
          ? 'No hay usuarios sin billetera.'
          : 'Usuarios sin billetera.',
    },
    {
      name: 'saldo',
      label: 'Saldo (CLP)',
      type: 'number',
      step: 0.01,
      required: true,
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

  const validate = (form, editando) => {
    if (!editando && !form.idUsuario) {
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
      description="Saldo de cada usuario."
      entityName="billetera"
      createLabel="Nueva billetera"
      service={billeteraService}
      onCambio={() => setRevision((valor) => valor + 1)}
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
