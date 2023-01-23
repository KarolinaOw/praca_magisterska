import {Component} from '@angular/core';
import {Color, ColorScheme, LogoParameters, SequenceType} from "./model";
import {Title} from "@angular/platform-browser";
import {LogoService} from "./logo.service";

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
export class AppComponent {

  logoParameters: LogoParameters = new LogoParameters();
  rawData: string = '';
  sequenceTypes: SequenceType[] = ['auto', 'aminoAcid', 'dna', 'rna'];
  colorSchemes: ColorScheme[] = ['auto', 'monochrome'];
  submitted = false;

  constructor(private titleService: Title, private logoService: LogoService) {
    this.titleService.setTitle($localize`${this.titleService}`);
  }

  onSubmit() {
    console.log(JSON.stringify(this.logoParameters));
    this.submitted = true;
    this.logoService.createLogoFromRawData(this.logoParameters, this.rawData);
  }

  onColorSelectionChange(entry: ColorScheme): void {
    this.logoParameters.colors = entry;
  }

  toDefaultValues(): void {
    this.logoParameters = new LogoParameters();
  }

}
