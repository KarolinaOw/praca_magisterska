import { Component } from '@angular/core';

type OutputFormat = 'vector' | 'JPG' | 'PDF';
type SequenceType = 'auto' | 'protein' | 'dna' | 'rna';
type LogoUnit = 'bits' | 'probability' | 'nats' | 'kT';
type ColorScheme = 'auto' | 'monochrome' | 'custom';
type Color = 'red' | 'blue' | 'green' | 'yellow' | 'purple' | 'orange';

interface SymbolColor{
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
  title : 'web';
  input_data: string;
  output_format : OutputFormat;
  logo_size_x : number;
  logo_size_y : number;
  logo_title : string;
  hide_axis_x : boolean;
  hide_axis_y : boolean;
  axis_x_title : string;
  axis_y_title : string;
  axis_y_scale: number;
  sequence_type : SequenceType;
  first_position_number: number;
  unit: LogoUnit;
  color_scheme: ColorScheme;
  symbol_colors: SymbolColor[] = [];

  onSubmit() {

  }

}
