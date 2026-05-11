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
  readonly theme = themeQuartz.withPart(colorSchemeDark).withParams({
    backgroundColor: 'oklch(12.9% 0.042 264.695)',   // slate-950
    headerBackgroundColor: 'oklch(20.8% 0.042 265.755)', // slate-900
    oddRowBackgroundColor: 'oklch(20.8% 0.042 265.755)', // slate-900
    borderColor: 'oklch(37.2% 0.044 257.287)',        // slate-700
    foregroundColor: 'oklch(96.8% 0.007 247.896)',    // slate-100
    // headerForegroundColor: 'oklch(96.8% 0.007 247.896)', // slate-100
    accentColor: 'oklch(68.5% 0.169 237.323)',        // sky-500
    rowHoverColor: 'oklch(27.9% 0.041 260.031)',      // slate-800
  });
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
