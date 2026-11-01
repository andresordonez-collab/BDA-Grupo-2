import { useEffect, useMemo, useState } from 'react';
import { Chip } from '@mui/material';
import EntityCrudPage from '../../components/common/EntityCrudPage';
import usuarioService from '../../services/usuarioService';
import rolService from '../../services/rolService';
import { formatearFechaHora } from '../../utils/formato';

export default function UsuariosPage() {
  const [roles, setRoles] = useState([]);

  useEffect(() => {
    let activo = true;

    rolService
      .getAll()
      .then(({ data }) => {
        if (activo) {
          setRoles(Array.isArray(data) ? data : []);
        }
      })
      .catch(() => {
        if (activo) {
          setRoles([]);
        }
      });

    return () => {
      activo = false;
    };
  }, []);

  const mapaRoles = useMemo(
    () =>
      new Map(
        roles.map((rol) => [
          String(rol.idRol),
          rol.nombreRol,
        ])
      ),
    [roles]
  );

  const opcionesRol = useMemo(
    () =>
      roles.map((rol) => ({
        value: String(rol.idRol),
        label: rol.nombreRol,
      })),
    [roles]
  );

  const columns = [
    {
      field: 'idUsuario',
      label: 'ID',
      align: 'right',
    },
    {
      field: 'nombreUsuario',
      label: 'Nombre',
    },
    {
      field: 'email',
      label: 'Email',
    },
    {
      field: 'idRol',
      label: 'Rol',
      render: (item) =>
        mapaRoles.get(String(item.idRol)) ?? `#${item.idRol}`,
    },
    {
      field: 'usuarioActivo',
      label: 'Estado',
      render: (item) => (
        <Chip
          size="small"
          color={item.usuarioActivo ? 'success' : 'default'}
          variant={item.usuarioActivo ? 'filled' : 'outlined'}
          label={item.usuarioActivo ? 'Activo' : 'Inactivo'}
        />
      ),
    },
    {
      field: 'fechaRegistro',
      label: 'Registro',
      render: (item) => formatearFechaHora(item.fechaRegistro),
    },
  ];

  const fields = [
    {
      name: 'nombreUsuario',
      label: 'Nombre completo',
      type: 'text',
      required: true,
    },
    {
      name: 'email',
      label: 'Correo electrónico',
      type: 'email',
      required: true,
    },
    {
      name: 'idRol',
      label: 'Rol',
      type: 'select',
      options: opcionesRol,
      required: true,
    },
    {
      name: 'usuarioActivo',
      label: 'Usuario activo',
      type: 'checkbox',
    },
    {
      name: 'passwordHash',
      label: 'Contraseña',
      type: 'password',
      helperText:
        'Obligatoria al crear. Déjala vacía al editar para conservar la actual.',
    },
  ];

  const initialValues = {
    nombreUsuario: '',
    email: '',
    idRol: '',
    usuarioActivo: true,
    passwordHash: '',
  };

  const toForm = (item) => ({
    nombreUsuario: item.nombreUsuario ?? '',
    email: item.email ?? '',
    idRol:
      item.idRol !== null && item.idRol !== undefined
        ? String(item.idRol)
        : '',
    usuarioActivo: Boolean(item.usuarioActivo),
    passwordHash: '',
  });

  const toPayload = (form) => {
    const payload = {
      idRol: Number(form.idRol),
      nombreUsuario: form.nombreUsuario.trim(),
      email: form.email.trim(),
      usuarioActivo: Boolean(form.usuarioActivo),
    };

    // En edición, si la contraseña está vacía se omite.
    // El backend conserva el hash actual.
    if (form.passwordHash) {
      payload.passwordHash = form.passwordHash;
    }

    return payload;
  };

  const validate = (form, editando) => {
    if (!form.nombreUsuario.trim()) {
      return 'El nombre es obligatorio.';
    }

    if (!form.email.trim() || !form.email.includes('@')) {
      return 'Ingresa un correo válido.';
    }

    if (!form.idRol) {
      return 'Selecciona un rol.';
    }

    if (!editando && !form.passwordHash) {
      return 'La contraseña es obligatoria al crear un usuario.';
    }

    if (form.passwordHash && form.passwordHash.length < 6) {
      return 'La contraseña debe tener al menos 6 caracteres.';
    }

    return '';
  };

  return (
    <EntityCrudPage
      title="Usuarios"
      description="Administra las cuentas, roles y estado de acceso. Para dar de baja a un usuario, edítalo y desmarca Usuario activo; así se conserva su historial."
      entityName="usuario"
      createLabel="Nuevo usuario"
      service={usuarioService}
      idField="idUsuario"
      columns={columns}
      fields={fields}
      initialValues={initialValues}
      toForm={toForm}
      toPayload={toPayload}
      validate={validate}
      canDelete={false}
      getLabel={(item) =>
        `el usuario ${item?.nombreUsuario ?? ''}`
      }
    />
  );
}
