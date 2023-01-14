export class LogoRequest {

  seqType: SequenceType = "auto";
  firstPosNumber: number = 1;
  title: string = "Sequence Logo";
  xAxis: string = "Information content [bits]";
  yAxis: string = "Position";
  colors: ColorScheme = "auto";

}

export type SequenceType = 'auto' | 'aminoAcid' | 'dna' | 'rna';
export type ColorScheme = 'auto' | 'monochrome';
export type Color = 'red' | 'blue' | 'green' | 'yellow' | 'orange' | 'black';
