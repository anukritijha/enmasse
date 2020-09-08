/*
 * Copyright 2020, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import React from "react";
import { useQuery } from "@apollo/react-hooks";
import { POLL_INTERVAL, FetchPolicy } from "constant";
import { IIoTDevicesResponse } from "schema/iot_device";
import {
  RETURN_ALL_DEVICES_FOR_IOT_PROJECT,
  DELETE_IOT_DEVICE,
  TOGGLE_IOT_DEVICE_STATUS
} from "graphql-module/queries/iot_device";
import {
  DeviceList,
  IDevice,
  IDeviceFilter
} from "modules/iot-device/components";
import {
  getHeaderForDialog,
  getDetailForDialog,
  getInitialFilter
} from "modules/iot-device/utils";
import {
  IRowData,
  SortByDirection,
  ISortBy,
  IExtraColumnData
} from "@patternfly/react-table";
import { compareObject } from "utils";
import { DialogTypes } from "constant";
import { useMutationQuery } from "hooks";
import { Loading } from "use-patternfly";
import { useStoreContext, MODAL_TYPES, types } from "context-state-reducer";
import { NoResultFound } from "components";

export interface IDeviceListContainerProps {
  page: number;
  perPage: number;
  setTotalDevices: (val: number) => void;
  onSelectDevice: (
    data: IDevice,
    isSelected: boolean,
    isAllSelected?: boolean
  ) => void;
  selectedDevices: IDevice[];
  areAllDevicesSelected: boolean;
  selectAllDevices: (devices: IDevice[]) => void;
  sortValue?: ISortByWrapper;
  setSortValue: (value: ISortByWrapper) => void;
  appliedFilter: IDeviceFilter;
  resetFilter: () => void;
  projectname: string;
  namespace: string;
  selectedColumns: string[];
  setIsAllSelected: (value: boolean) => void;
  onSelectAllDevices: (isSelected: boolean) => void;
}
export interface ISortByWrapper extends ISortBy {
  property?: string;
}

export const DeviceListContainer: React.FC<IDeviceListContainerProps> = ({
  page,
  perPage,
  setTotalDevices,
  onSelectDevice,
  selectedDevices,
  areAllDevicesSelected,
  selectAllDevices,
  sortValue,
  setSortValue,
  appliedFilter,
  resetFilter,
  projectname,
  namespace,
  setIsAllSelected,
  selectedColumns,
  onSelectAllDevices
}) => {
  const { dispatch } = useStoreContext();

  const { loading, data } = useQuery<IIoTDevicesResponse>(
    RETURN_ALL_DEVICES_FOR_IOT_PROJECT(
      page,
      perPage,
      projectname,
      namespace,
      sortValue,
      appliedFilter
    ),
    { pollInterval: POLL_INTERVAL, fetchPolicy: FetchPolicy.NETWORK_ONLY }
  );

  const { total, devices } = data?.devices || {
    devices: [],
    total: 0
  };

  const [setDeleteDeviceQueryVariables] = useMutationQuery(DELETE_IOT_DEVICE, [
    "devices_for_iot_project"
  ]);

  const [
    setUpdateDeviceQueryVariables
  ] = useMutationQuery(TOGGLE_IOT_DEVICE_STATUS, ["devices_for_iot_project"]);

  if (loading) {
    return <Loading />;
  }

  setTotalDevices(total);

  const onConfirmDeleteDevice = async (deviceId: string) => {
    const variable = {
      deviceId,
      iotproject: {
        name: projectname,
        namespace
      }
    };
    await setDeleteDeviceQueryVariables(variable);
  };

  const onConfirmToggleDeviceStatus = async (data: any) => {
    const variable = {
      a: { name: projectname, namespace },
      b: [data.deviceId],
      status: data.status
    };
    await setUpdateDeviceQueryVariables(variable);
  };

  const deleteDevice = (row: IRowData) => {
    const { deviceId } = row.originalData;
    dispatch({
      type: types.SHOW_MODAL,
      modalType: MODAL_TYPES.DELETE_IOT_DEVICE,
      modalProps: {
        onConfirm: onConfirmDeleteDevice,
        selectedItems: [deviceId],
        option: DialogTypes.DELETE,
        data: deviceId,
        detail: getDetailForDialog([{ deviceId }], DialogTypes.DELETE),
        header: getHeaderForDialog([{ deviceId }], DialogTypes.DELETE),
        confirmButtonLabel: "Delete",
        iconType: "danger"
      }
    });
  };

  const toggleDeviceStatus = (row: IRowData, status: boolean) => {
    const { deviceId } = row.originalData;
    const dialogType: string = status
      ? DialogTypes.ENABLE
      : DialogTypes.DISABLE;
    dispatch({
      type: types.SHOW_MODAL,
      modalType: MODAL_TYPES.UPDATE_DEVICE_STATUS,
      modalProps: {
        onConfirm: onConfirmToggleDeviceStatus,
        selectedItems: [deviceId],
        option: dialogType,
        data: { deviceId, status },
        detail: getDetailForDialog([{ deviceId }], dialogType),
        header: getHeaderForDialog([{ deviceId }], dialogType),
        confirmButtonLabel: dialogType
      }
    });
  };

  const actionResolver = (rowData: IRowData) => {
    const enabled = rowData?.originalData?.enabled;
    return [
      {
        title: "Delete",
        onClick: () => deleteDevice(rowData)
      },
      {
        title: enabled ? "Disable" : "Enable",
        onClick: () => toggleDeviceStatus(rowData, enabled ? false : true)
      }
    ];
  };

  const onSort = (
    _event: any,
    index: number,
    direction: SortByDirection,
    extraData: IExtraColumnData
  ) => {
    setSortValue({
      index: index,
      direction: direction,
      property: extraData.property
    });
  };

  const deviceRows: IDevice[] =
    devices?.map(({ deviceId, registration, status, credentials }) => {
      const { enabled, via, viaGroups } = registration || {};
      return {
        deviceId,
        enabled,
        via,
        ...(credentials?.trim() !== "" && {
          credentials: JSON.parse(credentials)
        }),
        viaGroups,
        ...status,
        selected:
          selectedDevices.filter(device =>
            compareObject({ deviceId }, { deviceId: device.deviceId })
          ).length === 1
      };
    }) || [];

  if (areAllDevicesSelected && selectedDevices.length !== devices?.length) {
    selectAllDevices(deviceRows || []);
  }

  if (deviceRows.every(row => row.selected === true)) {
    setIsAllSelected(true);
  }

  const onSelect = (device: IDevice, isSelected: boolean) => {
    if (!areAllDevicesSelected && isSelected) {
      if (selectedDevices.length === deviceRows.length - 1) {
        let allSelected = true;
        for (let row of deviceRows) {
          for (let selectedDevice of selectedDevices) {
            if (compareObject(row.deviceId, selectedDevice.deviceId)) {
              if (device.deviceId === row.deviceId) {
                allSelected = true;
              } else if (row.selected === false) allSelected = false;
              break;
            }
          }
        }
        if (allSelected) {
          onSelectDevice(device, isSelected, true);
        }
      }
    }
    onSelectDevice(device, isSelected);
  };

  return (
    <>
      <DeviceList
        deviceRows={deviceRows}
        onSelectDevice={onSelect}
        actionResolver={actionResolver}
        onSort={onSort}
        sortBy={sortValue}
        selectedColumns={selectedColumns}
        onSelectAllDevices={onSelectAllDevices}
      />
      {total === 0 && !compareObject(appliedFilter, getInitialFilter()) ? (
        <NoResultFound clearFilters={resetFilter} />
      ) : (
        " "
      )}
    </>
  );
};
