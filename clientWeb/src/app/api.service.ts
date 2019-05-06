import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'https://webhooks.mongodb-stitch.com/api/client/v2.0/app/seriot-xsfbc/service/Check/incoming_webhook/getLast50';
  public records: Map<string, string>[] ;
  public fields: string[];
  public addresses: number[];
  public chartFields: string[];
  constructor(private http: HttpClient) {
    this.records = [];
    this.fields = [];
    this.addresses = [];
    this.chartFields = [];
    setInterval(
      () => {
        http.get(this.baseUrl).toPromise().then(
          (v: Array<any>) => {
            let recordsNew = [];
            let fieldsNew = [];
            let addressesNew = [];
            let chartFieldsNew = [];
            for ( const e of v) {
              const newEl = new Map<string, string>();
              delete e._id;
              Object.keys(e).forEach(
                (key) => {
                  if (fieldsNew.indexOf(key) === -1) {
                    fieldsNew.push(key);
                    if (key !== 'addr' && key !== 'time') {
                      chartFieldsNew.push(key);
                    }
                  }
                  if (key === 'time') {
                    newEl.set(key, new Date(+e[key].$numberLong).toTimeString());
                  } else {
                    newEl.set(key, e[key].$numberLong);
                  }
                }
              );
              if ( addressesNew.indexOf(+newEl.get('addr')) === -1) {
                addressesNew.push(+newEl.get('addr'));
              }

              recordsNew.push(newEl);
            }
            this.records = recordsNew;
            this.chartFields = chartFieldsNew;
            this.fields = fieldsNew;
            this.addresses = addressesNew;
          }
        );


      }, 1000
    );
  }
}
