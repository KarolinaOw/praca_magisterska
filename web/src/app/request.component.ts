import {Component} from "@angular/core";
import {ColorScheme, LogoParameters, LogoRequestStatus, SequenceType} from "./model";
import {Title} from "@angular/platform-browser";
import {LogoService} from "./logo.service";
import {interval, startWith, switchMap, takeWhile} from "rxjs";
import {Router} from "@angular/router";

@Component({
  selector: 'app-request',
  templateUrl: './request.component.html',
  styleUrls: ['./request.component.css']
})
export class RequestComponent {

  logoParameters: LogoParameters = new LogoParameters();
  rawData: string = '';
  sequenceTypes: SequenceType[] = ['auto', 'aminoAcid', 'dna', 'rna'];
  colorSchemes: ColorScheme[] = ['auto', 'monochrome'];
  inProgress = false;

  constructor(private titleService: Title,
              private router: Router,
              private logoService: LogoService) {
    this.titleService.setTitle($localize`${this.titleService}`);
  }

  onSubmit() {
    console.log(JSON.stringify(this.logoParameters));
    this.inProgress = true;
    this.logoService.createLogoFromRawData(this.logoParameters, this.rawData)
      .subscribe(logoRequestId => this.watchLogoRequest(logoRequestId));
  }

  onColorSelectionChange(entry: ColorScheme): void {
    this.logoParameters.colors = entry;
  }

  toDefaultValues(): void {
    this.logoParameters = new LogoParameters();
  }

  private watchLogoRequest(logoRequestId: number) {
    interval(2000)
      .pipe(
        startWith(0),
        switchMap(() => this.logoService.getLogoRequestStatus(logoRequestId)),
        takeWhile(() => this.inProgress)
      )
      .subscribe(status => {
        if (status === LogoRequestStatus.FINISHED) {
          this.inProgress = false;
          this.router.navigate(['results', logoRequestId]);
        }
      });
  }
}
