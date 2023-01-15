import { Component } from '@angular/core';
import {Color, ColorScheme, LogoRequest, SequenceType} from "./logoRequest";
import {Title} from "@angular/platform-browser";

interface SymbolColor {
  symbol: string;
  color: Color;
  rgb: string;
}


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent{

  constructor(private titleService: Title) {
    this.titleService.setTitle($localize `${this.titleService}`);
  }
  logoRequest: LogoRequest = new LogoRequest();

  sequenceTypes: SequenceType[] = ['auto', 'aminoAcid', 'dna', 'rna'];
  colorSchemes: ColorScheme[] = ['auto', 'monochrome'];
  submitted = false;
  onSubmit() {
    console.log(JSON.stringify(this.logoRequest));
    this.submitted = true;
    // const req = http.get<>('/api/');
    // req.subscribe();

  }

  onColorSelectionChange(entry: ColorScheme): void {
    this.logoRequest.colors = entry;
  }

  toDefaultValues(): void {
    this.logoRequest = new LogoRequest();
  }

}
