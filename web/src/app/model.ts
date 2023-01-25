export type SequenceType = 'auto' | 'aminoAcid' | 'dna' | 'rna';
export type ColorScheme = 'auto' | 'monochrome';
export type Color = 'red' | 'blue' | 'green' | 'yellow' | 'orange' | 'black';

export class LogoParameters {
  seqType: SequenceType = "auto";
  firstPosNumber: number = 1;
  title: string = "Sequence Logo";
  xAxis: string = "Information content [bits]";
  yAxis: string = "Position";
  colors: ColorScheme = "auto";
}

export enum LogoRequestStatus {
  NEW = 'NEW',
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  FINISHED = 'FINISHED'
}

export class LogoRequestInput {
  fileId: string;
  params: LogoParameters;
}

export class LogoRequest {
  id: number;
  fileId: string;
  params: LogoParameters;
  status: LogoRequestStatus;
}

export class DataFileHandle {
  fileId: string;
}
