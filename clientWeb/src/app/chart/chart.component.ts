import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import { Chart } from 'chart.js';
import {ApiService} from '../api.service';
import {ActivatedRoute, Params} from '@angular/router';

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnInit {
  @Input() field: string;
  @ViewChild('canvasElement') canvasRef: ElementRef;
  chart;
  addr = 2;
  constructor(private apiService: ApiService, private activatedRoute: ActivatedRoute) { }

  ngOnInit() {
    const ctx = this.canvasRef.nativeElement.getContext('2d');
    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: [],
        datasets: [
          {
            label: this.field,
            data: [],
            fill: false,
            lineTension: 0
          }
        ]
      }
    });

    this.activatedRoute.params.subscribe(
      (params: Params) => {
        this.chart.data.labels.length = 0;
        this.chart.data.datasets[0].data.length = 0;
        let i = 0;
        for (const rec of this.apiService.records) {
          if (+rec.get('addr') == params.id) {
            this.chart.data.labels.push(i++);
            this.chart.data.datasets[0].data.push(rec.get(this.field));

          }
        }
        this.chart.update();

    });

  }

}
