import { Component } from '@angular/core';
import { AgGridAngular } from 'ag-grid-angular';
import {
  ColDef,
  colorSchemeDark,
  IServerSideDatasource,
  IServerSideGetRowsParams,
  themeQuartz,
} from 'ag-grid-community';

interface RowData {
  id: number;
  type: string;
  payload: string;
  ts: string;
}

const TOTAL_ROWS = 10_000;
const TYPES = ['ALPHA', 'BETA', 'ENTITY', 'REF'];

function generateRows(start: number, end: number): RowData[] {
  return Array.from({ length: end - start }, (_, i) => {
    const id = start + i + 1;
    return {
      id,
      type: TYPES[id % TYPES.length],
      payload: `payload-${id.toString(36)}-${Math.random().toString(36).slice(2, 8)}`,
      ts: new Date(Date.now() - (TOTAL_ROWS - id) * 1_000).toISOString(),
    };
  });
}

@Component({
  selector: 'app-root',
  imports: [AgGridAngular],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  readonly theme = themeQuartz.withPart(colorSchemeDark);
  readonly totalRows = TOTAL_ROWS;

  readonly columnDefs: ColDef<RowData>[] = [
    { field: 'id', width: 100 },
    { field: 'type', width: 120 },
    { field: 'payload', flex: 1, minWidth: 200 },
    { field: 'ts', headerName: 'Timestamp', width: 240 },
  ];

  readonly defaultColDef: ColDef = {
    resizable: true,
  };

  readonly datasource: IServerSideDatasource = {
    getRows(params: IServerSideGetRowsParams): void {
      const start = params.request.startRow ?? 0;
      const end = Math.min(params.request.endRow ?? 100, TOTAL_ROWS);
      setTimeout(() => {
        params.success({ rowData: generateRows(start, end), rowCount: TOTAL_ROWS });
      }, 150);
    },
  };
}
