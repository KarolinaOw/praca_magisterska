import {Injectable} from '@angular/core';
import {environment} from "./environment";
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {LogoParameters, LogoRequest} from './model';
import {mergeMap, Observable} from 'rxjs';
import {DataFileHandle} from "./dataFileHandle";


@Injectable({
  providedIn: 'root'
})
export class LogoService {

  URL = environment.API_URL; // endpoint URL

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  }

  constructor(private httpClient: HttpClient) {
  }

  createLogoFromRawData(logoParameters: LogoParameters, data: string): void {
    this.uploadDataRawRequest(data)
      .pipe(mergeMap(fileHandle => this.createLogoRequest(logoParameters, fileHandle.fileId)))
      .subscribe(response => this.getOutputData(response.id)
        .subscribe(data => console.log(data)));
  }

  private uploadDataRawRequest(data: string): Observable<DataFileHandle> {
    return this.httpClient.post<DataFileHandle>('/api/data/raw', data);
  }

  private createLogoRequest(logoParameters: LogoParameters, fileId: string): Observable<LogoRequest> {
    return this.httpClient.post<LogoRequest>('/api/logo-requests', {fileId, params: logoParameters});
  }

  private getOutputData(logoRequestId: number): Observable<string> {
    return this.httpClient.get<string>(`/api/data/${logoRequestId}/output`);
  }
}
