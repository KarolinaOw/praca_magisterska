import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {LogoParameters, LogoRequest, LogoRequestStatus} from './model';
import {map, mergeMap, Observable} from 'rxjs';
import {DataFileHandle} from "./dataFileHandle";


@Injectable({
  providedIn: 'root'
})
export class LogoService {

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  }

  constructor(private httpClient: HttpClient) {
  }

  createLogoFromRawData(logoParameters: LogoParameters, data: string): Observable<number> {
    return this.uploadDataRawRequest(data)
      .pipe(mergeMap(fileHandle => this.createLogoRequest(logoParameters, fileHandle.fileId)))
      .pipe(map(response => response.id));
  }

  getLogoRequestStatus(logoRequestId: number): Observable<LogoRequestStatus> {
    return this.httpClient.get<LogoRequest>(`/api/logo-requests/${logoRequestId}`)
      .pipe(map(response => response.status));
  }

  getOutputData(logoRequestId: number): Observable<string> {
    return this.httpClient.get(`/api/data/${logoRequestId}/output`, {responseType: "text"});
  }

  private uploadDataRawRequest(data: string): Observable<DataFileHandle> {
    return this.httpClient.post<DataFileHandle>('/api/data/raw', data);
  }

  private createLogoRequest(logoParameters: LogoParameters, fileId: string): Observable<LogoRequest> {
    return this.httpClient.post<LogoRequest>('/api/logo-requests', {fileId, params: logoParameters});
  }
}
