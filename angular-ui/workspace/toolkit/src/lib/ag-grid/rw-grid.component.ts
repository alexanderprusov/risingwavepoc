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
    backgroundColor: 'var(--p-surface-950)',
    headerBackgroundColor: 'var(--p-surface-900)',
    oddRowBackgroundColor: 'var(--p-surface-900)',
    borderColor: 'var(--p-surface-700)',
    foregroundColor: 'var(--p-surface-0)',
    accentColor: 'var(--p-primary-color)',
    rowHoverColor: 'var(--p-surface-800)',
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
