import { LOCALE_ID, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import {LogoService} from "./logo.service";
import {HttpClientModule} from "@angular/common/http";
import {RouterModule, Routes} from "@angular/router";
import {ResultsComponent} from "./results.component";
import {RequestComponent} from "./request.component";

const routes: Routes = [
  {path: '', component: RequestComponent},
  {path: 'results/:id', component: ResultsComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    ResultsComponent,
    RequestComponent
  ],
  imports: [
    RouterModule.forRoot(routes),
    BrowserModule,
    CommonModule,
    FormsModule,
    HttpClientModule
  ],
  providers: [{provide: LOCALE_ID, useValue: "pl"}, LogoService],
  bootstrap: [AppComponent]
})
export class AppModule { }
