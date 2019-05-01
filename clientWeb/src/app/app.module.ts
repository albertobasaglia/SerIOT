import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NoChartComponent } from './no-chart/no-chart.component';
import { ChartComponent } from './chart/chart.component';
import { TableComponent } from './table/table.component';
import {HttpClientModule} from '@angular/common/http';
import { ChartsComponent } from './charts/charts.component';

@NgModule({
  declarations: [
    AppComponent,
    NoChartComponent,
    ChartComponent,
    TableComponent,
    ChartsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
