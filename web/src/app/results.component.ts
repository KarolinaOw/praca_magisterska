import {Component, ElementRef, ViewChild} from "@angular/core";
import {LogoService} from "./logo.service";
import {ActivatedRoute} from "@angular/router";
import * as d3 from "d3";

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent {

  @ViewChild("logoContent") logoContent: ElementRef;

  constructor(private route: ActivatedRoute,
              private logoService: LogoService) {
  }

  ngAfterViewInit() {
    this.route.paramMap.subscribe(params =>
      this.logoService.getOutputData(parseInt(params.get('id') as string))
        .subscribe(data => {
          this.logoContent.nativeElement.innerHTML = data;
          this.initZoom();
        }));
  }

  initZoom() {
    const svg = d3.select("svg");
    const zoomFn = d3.zoom().scaleExtent([1,40]).on('zoom', (event) => {
      svg.attr("transform", event.transform);
    });
    // @ts-ignore
    svg.call(zoomFn);
  }
}
