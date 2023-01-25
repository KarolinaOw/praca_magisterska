import {Component} from "@angular/core";
import {LogoService} from "./logo.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html'
})
export class ResultsComponent {

  outputData: string;

  constructor(private route: ActivatedRoute,
              private logoService: LogoService) {
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params =>
      this.logoService.getOutputData(parseInt(params.get('id') as string))
        .subscribe(data => this.outputData = data));
  }
}
