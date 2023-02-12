import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {LogoParameters, LogoRequest, LogoRequestStatus, DataFileHandle, UploadableDataFileHandle} from './model';
import {map, mergeMap, Observable} from 'rxjs';


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

  createLogoFromFile(logoParameters: LogoParameters, file: File): Observable<number> {
    return this.uploadDataFileRequest(file)
      .pipe(mergeMap(fileHandle => this.createLogoRequest(logoParameters, fileHandle.fileId)))
      .pipe(map(response => response.id));
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
    return this.httpClient.get(`/api/data/${logoRequestId}/logo`, {responseType: "text"});
  }

  private uploadDataFileRequest(file: File): Observable<DataFileHandle> {
    return this.httpClient.post<UploadableDataFileHandle>('/api/data/file', null)
      .pipe(mergeMap(uploadable => this.uploadObject(uploadable, file)));
  }

  private uploadObject(handle: UploadableDataFileHandle, file: File): Observable<DataFileHandle> {
    const headers: HttpHeaders = new HttpHeaders();
    headers.set('Content-Type', 'application/octet-stream');
    return this.httpClient.put(handle.signedUrl, file, {headers})
      .pipe(map(() => {
          return {fileId: handle.fileId}
        }));
  }

  private uploadDataRawRequest(data: string): Observable<DataFileHandle> {
    return this.httpClient.post<DataFileHandle>('/api/data/raw', data);
  }

  private createLogoRequest(logoParameters: LogoParameters, fileId: string): Observable<LogoRequest> {
    return this.httpClient.post<LogoRequest>('/api/logo-requests', {fileId, params: logoParameters});
  }
}
