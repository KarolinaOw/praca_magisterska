import { Injectable } from '@angular/core';
import {environment} from "./environment";
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { LogoRequest } from './logoRequest';
import { Observable } from 'rxjs';


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

  constructor(private httpClient: HttpClient) { }

  create(logoRequest: any): Observable<any> {
    return this.httpClient.post<LogoRequest>(`${this.URL}`, logoRequest, this.httpOptions);
  }

}
