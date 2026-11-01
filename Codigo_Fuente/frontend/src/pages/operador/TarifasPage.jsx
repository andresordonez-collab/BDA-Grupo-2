import { useEffect, useMemo, useState } from 'react';
import { Chip } from '@mui/material';
import EntityCrudPage from '../../components/common/EntityCrudPage';
import tarifaService from '../../services/tarifaService';
import tipoVehiculoService from '../../services/tipoVehiculoService';
import {
  formatoCLP,
  formatearFechaHora,
} from '../../utils/formato';

function fechaLocalActual() {
  const ahora = new Date();
  const dos = (numero) => String(numero).padStart(2, '0');

  return `${ahora.getFullYear()}-${dos(
    ahora.getMonth() + 1
  )}-${dos(ahora.getDate())}T${dos(ahora.getHours())}:${dos(
    ahora.getMinutes()
  )}`;
}

export default function TarifasPage() {
  const [tipos, setTipos] = useState([]);

  useEffect(() => {
    let activo = true;

    tipoVehiculoService
      .getAll()
      .then(({ data }) => {
        if (activo) {
          setTipos(Array.isArray(data) ? data : []);
        }
      })
      .catch(() => {
        if (activo) {
          setTipos([]);
        }
      });

    return () => {
      activo = false;
    };
  }, []);

  const mapaTipos = useMemo(
    () =>
      new Map(
        tipos.map((tipo) => [
          String(tipo.idTipoVehiculo),
          tipo.nombreTipoVehiculo,
        ])
      ),
    [tipos]
  );

  const opcionesTipo = useMemo(
    () =>
      tipos.map((tipo) => ({
        value: String(tipo.idTipoVehiculo),
        label: tipo.nombreTipoVehiculo,
      })),
    [tipos]
  );

  const columns = [
    {
      field: 'idTarifa',
      label: 'ID',
      align: 'right',
    },
    {
      field: 'idTipoVehiculo',
      label: 'Tipo',
      render: (item) =>
        mapaTipos.get(String(item.idTipoVehiculo)) ??
        `#${item.idTipoVehiculo}`,
    },
    {
      field: 'valorDesbloqueo',
      label: 'Desbloqueo',
      align: 'right',
      render: (item) =>
        formatoCLP.format(Number(item.valorDesbloqueo ?? 0)),
    },
    {
      field: 'valorMinuto',
      label: 'Por minuto',
      align: 'right',
      render: (item) =>
        formatoCLP.format(Number(item.valorMinuto ?? 0)),
    },
    {
      field: 'vigenteDesde',
      label: 'Vigente desde',
      render: (item) => formatearFechaHora(item.vigenteDesde),
    },
    {
      field: 'activa',
      label: 'Estado',
      render: (item) => (
        <Chip
          size="small"
          color={item.activa ? 'success' : 'default'}
          label={item.activa ? 'Activa' : 'Inactiva'}
        />
      ),
    },
  ];

  const fields = [
    {
      name: 'idTipoVehiculo',
      label: 'Tipo de vehículo',
      type: 'select',
      options: opcionesTipo,
      required: true,
    },
    {
      name: 'valorDesbloqueo',
      label: 'Valor de desbloqueo (CLP)',
      type: 'number',
      min: 0,
      step: 1,
      required: true,
    },
    {
      name: 'valorMinuto',
      label: 'Valor por minuto (CLP)',
      type: 'number',
      min: 0,
      step: 1,
      required: true,
    },
    {
      name: 'vigenteDesde',
      label: 'Vigente desde',
      type: 'datetime-local',
      required: true,
    },
    {
      name: 'activa',
      label: 'Activa',
      type: 'checkbox',
    },
  ];

  const initialValues = {
    idTipoVehiculo: '',
    valorDesbloqueo: '0',
    valorMinuto: '',
    vigenteDesde: fechaLocalActual(),
    activa: true,
  };

  const toForm = (item) => ({
    idTipoVehiculo:
      item.idTipoVehiculo !== null &&
      item.idTipoVehiculo !== undefined
        ? String(item.idTipoVehiculo)
        : '',
    valorDesbloqueo:
      item.valorDesbloqueo !== null &&
      item.valorDesbloqueo !== undefined
        ? String(item.valorDesbloqueo)
        : '0',
    valorMinuto:
      item.valorMinuto !== null &&
      item.valorMinuto !== undefined
        ? String(item.valorMinuto)
        : '',
    vigenteDesde: item.vigenteDesde
      ? String(item.vigenteDesde).slice(0, 16)
      : fechaLocalActual(),
    activa: Boolean(item.activa),
  });

  const toPayload = (form) => ({
    idTipoVehiculo: Number(form.idTipoVehiculo),
    valorDesbloqueo: Number(form.valorDesbloqueo || 0),
    valorMinuto: Number(form.valorMinuto || 0),
    vigenteDesde: form.vigenteDesde
      ? `${form.vigenteDesde}:00`
      : null,
    activa: Boolean(form.activa),
  });

  const validate = (form) => {
    if (!form.idTipoVehiculo) {
      return 'Selecciona un tipo de vehículo.';
    }

    if (
      form.valorDesbloqueo === '' ||
      Number(form.valorDesbloqueo) < 0
    ) {
      return 'El valor de desbloqueo debe ser mayor o igual a 0.';
    }

    if (
      form.valorMinuto === '' ||
      Number(form.valorMinuto) < 0
    ) {
      return 'El valor por minuto debe ser mayor o igual a 0.';
    }

    if (!form.vigenteDesde) {
      return 'Selecciona la fecha de vigencia.';
    }

    return '';
  };

  return (
    <EntityCrudPage
      title="Tarifas"
      description="Administra los valores de desbloqueo y por minuto de cada tipo de vehículo."
      entityName="tarifa"
      createLabel="Nueva tarifa"
      service={tarifaService}
      idField="idTarifa"
      columns={columns}
      fields={fields}
      initialValues={initialValues}
      toForm={toForm}
      toPayload={toPayload}
      validate={validate}
      getLabel={(item) =>
        `la tarifa #${item?.idTarifa ?? ''}`
      }
    />
  );
}
