import { Component, Input, OnDestroy, OnInit, output } from '@angular/core';
import { AgGridAngular } from 'ag-grid-angular';
import { ColDef, colorSchemeDark, IServerSideDatasource, Theme, themeQuartz } from 'ag-grid-community';
import { ServerSideDatasource } from './datasource';
import { WebsocketDataProvider } from '../server/websocket-data-provider';

@Component({
  selector: 'rw-grid',
  imports: [AgGridAngular],
  templateUrl: './rw-grid.component.html',
})
export class RwGridComponent implements OnInit, OnDestroy {
  @Input({ required: true }) url!: string;
  @Input({ required: true }) action!: string;
  @Input() params: Record<string, unknown> = {};

  readonly totalRowsChange = output<number>();

  readonly theme: Theme = themeQuartz.withPart(colorSchemeDark).withParams({
    backgroundColor: 'oklch(12.9% 0.042 264.695)',
    headerBackgroundColor: 'oklch(20.8% 0.042 265.755)',
    oddRowBackgroundColor: 'oklch(20.8% 0.042 265.755)',
    borderColor: 'oklch(37.2% 0.044 257.287)',
    foregroundColor: 'oklch(96.8% 0.007 247.896)',
    accentColor: 'oklch(68.5% 0.169 237.323)',
    rowHoverColor: 'oklch(27.9% 0.041 260.031)',
  });

  readonly defaultColDef: ColDef = { resizable: true };

  columnDefs: ColDef[] = [];
  datasource: IServerSideDatasource | undefined;

  private provider: WebsocketDataProvider | null = null;

  async ngOnInit(): Promise<void> {
    this.provider = await WebsocketDataProvider.create(this.url);

    this.datasource = new ServerSideDatasource<Record<string, unknown>>(async (start, end) => {
      const result = await this.provider!.query(this.action, {
        startRow: start,
        endRow: end,
        ...this.params,
      });
      this.totalRowsChange.emit(result.rowCount);
      if (this.columnDefs.length === 0 && result.data.length > 0) {
        this.columnDefs = Object.keys(result.data[0]).map(field => ({ field }));
      }
      return { rows: result.data, totalRows: result.rowCount };
    });
  }

  ngOnDestroy(): void {
    this.provider?.close();
  }
}
