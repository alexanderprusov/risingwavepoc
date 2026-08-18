import { Observable, Subject } from 'rxjs';

export interface QueryParams {
  startRow: number;
  endRow: number;
  [key: string]: unknown;
}

export interface QueryResult<T> {
  data: T[];
  rowCount: number;
  elapsedMs: number;
}

interface QueryResultMessage<T> {
  type: 'QUERY_RESULT';
  id: string;
  data: T[];
  rowCount: number;
  elapsedMs: number;
}

interface ErrorMessage {
  type: 'ERROR';
  id: string;
  message: string;
}

interface AuthOkMessage {
  type: 'AUTH_OK';
  sessionId: string;
  username: string;
}

type ServerMessage<T> = QueryResultMessage<T> | ErrorMessage | AuthOkMessage;

type PendingEntry<T> = {
  resolve: (r: QueryResult<T>) => void;
  reject: (e: Error) => void;
};

export class WebsocketDataProvider<T = Record<string, unknown>> {
  private readonly pending = new Map<string, PendingEntry<T>>();
  private readonly push$ = new Subject<T[]>();

  readonly data$: Observable<T[]> = this.push$.asObservable();

  private constructor(private readonly socket: WebSocket) {
    socket.addEventListener('message', (event: MessageEvent) => {
      const msg = JSON.parse(event.data) as ServerMessage<T>;

      if (msg.type === 'QUERY_RESULT') {
        const entry = this.pending.get(msg.id);
        if (entry) {
          entry.resolve({ data: msg.data, rowCount: msg.rowCount, elapsedMs: msg.elapsedMs });
          this.pending.delete(msg.id);
        }
      } else if (msg.type === 'ERROR') {
        const entry = this.pending.get(msg.id);
        if (entry) {
          entry.reject(new Error(msg.message));
          this.pending.delete(msg.id);
        }
      }
    });

    socket.addEventListener('error', () => {
      const err = new Error('WebSocket error');
      this.pending.forEach(e => e.reject(err));
      this.pending.clear();
      this.push$.error(err);
    });

    socket.addEventListener('close', () => {
      const err = new Error('WebSocket closed');
      this.pending.forEach(e => e.reject(err));
      this.pending.clear();
      this.push$.complete();
    });
  }

  static create<T = Record<string, unknown>>(url: string): Promise<WebsocketDataProvider<T>> {
    return new Promise((resolve, reject) => {
      const socket = new WebSocket(url);
      socket.addEventListener('open', () => resolve(new WebsocketDataProvider<T>(socket)));
      socket.addEventListener('error', reject);
    });
  }

  query(action: string, params: QueryParams): Promise<QueryResult<T>> {
    const id = crypto.randomUUID();
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject });
      this.socket.send(JSON.stringify({ type: 'QUERY', id, action, params }));
    });
  }

  close(): void {
    this.socket.close();
  }
}
