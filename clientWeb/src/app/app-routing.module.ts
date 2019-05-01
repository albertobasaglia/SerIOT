import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {NoChartComponent} from './no-chart/no-chart.component';
import {ChartsComponent} from './charts/charts.component';
import {ChartComponent} from './chart/chart.component';

const routes = [
  {path: '', component: NoChartComponent},
  {path: 'chart/:id', component: ChartsComponent},
  {path: 'testchart', component: ChartComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
