import {Component} from "@angular/core";
import {ColorScheme, LogoParameters, LogoRequestStatus, SequenceType} from "./model";
import {LogoService} from "./logo.service";
import {interval, Observable, startWith, switchMap, takeWhile} from "rxjs";
import {Router} from "@angular/router";

@Component({
  selector: 'app-request',
  templateUrl: './request.component.html',
  styleUrls: ['./request.component.css']
})
export class RequestComponent {

  logoParameters: LogoParameters = new LogoParameters();
  rawData: string = '';
  file?: File;
  sequenceTypes: SequenceType[] = ['auto', 'dna', 'rna'];
  colorSchemes: ColorScheme[] = ['auto', 'monochrome'];
  inProgress = false;
  showError = false;

  constructor(private router: Router,
              private logoService: LogoService) {
  }

  onSubmit() {
    this.showError = false;
    this.inProgress = true;
    this.sendCreateLogoRequest()
      .subscribe({
        next: logoRequestId => this.watchLogoRequest(logoRequestId),
        error: () => {
          this.inProgress = false;
          this.showError = true;
        }
      });
  }

  private sendCreateLogoRequest(): Observable<number> {
    if (this.file) {
      return this.logoService.createLogoFromFile(this.logoParameters, this.file);
    }
    if (this.rawData && this.rawData.length > 0) {
      return this.logoService.createLogoFromRawData(this.logoParameters, this.rawData);
    }
    throw new Error("File not selected and no data provided");
  }

  onColorSelectionChange(entry: ColorScheme): void {
    this.logoParameters.colors = entry;
  }

  toDefaultValues(): void {
    this.logoParameters = new LogoParameters();
  }

  onFileSelected(event: Event) {
    const files = (<HTMLInputElement>event.target).files;
    if (files && files[0]) {
      this.file = files[0];
    }
  }

  private watchLogoRequest(logoRequestId: number) {
    interval(2000)
      .pipe(
        startWith(0),
        switchMap(() => this.logoService.getLogoRequestStatus(logoRequestId)),
        takeWhile(() => this.inProgress)
      )
      .subscribe(status => {
        console.log(status);
        if (status === LogoRequestStatus.FINISHED) {
          this.router.navigate(['results', logoRequestId])
            .then(() => this.inProgress = false);
        }
      });
  }
}
