import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'https://webhooks.mongodb-stitch.com/api/client/v2.0/app/seriot-xsfbc/service/Check/incoming_webhook/getAll';
  public records: Map<string, string>[] ;
  public fields: string[];
  public addresses: number[];
  public chartFields: string[];
  constructor(private http: HttpClient) {
    this.records = [];
    this.fields = [];
    this.addresses = [];
    this.chartFields = [];
    http.get(this.baseUrl).toPromise().then(
      (v: Array<any>) => {
        for ( const  e of v) {
          const newEl = new Map<string, string>();
          delete e._id;
          Object.keys(e).forEach(
            (key) => {
              if (this.fields.indexOf(key) === -1) {
                this.fields.push(key);
                if (key !== 'addr' && key !== 'time') {
                  this.chartFields.push(key);
                }
              }
              if (key === 'time') {
                newEl.set(key, new Date(+e[key].$numberLong).toTimeString());
              } else {
                newEl.set(key, e[key].$numberLong);
              }
            }
          );
          if ( this.addresses.indexOf(+newEl.get('addr')) === -1) {
            this.addresses.push(+newEl.get('addr'));
          }

          this.records.push(newEl);
        }
      }
    );
  }
}
