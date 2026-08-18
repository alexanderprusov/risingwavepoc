import { IServerSideDatasource, IServerSideGetRowsParams } from 'ag-grid-community';

export type RowsFetcher<T> = (
  start: number,
  end: number,
) => Promise<{ rows: T[]; totalRows: number }>;

export class ServerSideDatasource<T> implements IServerSideDatasource {
  constructor(private readonly fetcher: RowsFetcher<T>) {}

  getRows(params: IServerSideGetRowsParams): void {
    const start = params.request.startRow ?? 0;
    const end = params.request.endRow ?? start + 100;
    this.fetcher(start, end)
      .then(({ rows, totalRows }) => params.success({ rowData: rows, rowCount: totalRows }))
      .catch(() => params.fail());
  }
}
