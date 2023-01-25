import {Component} from '@angular/core';
import {Color, ColorScheme, LogoParameters, LogoRequestStatus, SequenceType} from "./model";
import {Title} from "@angular/platform-browser";
import {LogoService} from "./logo.service";
import {interval, startWith, switchMap, takeWhile} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent {
}
