import {Component} from '@angular/core';
import {ApiService} from './api.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent{
  title = 'clientWeb';
  constructor(private apiService: ApiService, private router: Router) {
  }
  onButtonClick(id: number) {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['chart', id]);
  }
}
