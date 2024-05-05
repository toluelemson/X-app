import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Currency} from 'src/app/Currency';

@Injectable({
  providedIn: 'root',
})
export class CurrencyService {

  private currencies: Currency[] = [];
  private lastUpdate: any;

  constructor(private http: HttpClient) {
  }

  public getCurrencies() {
    return this.currencies;
  }

  public getLastUpdate() {
    console.log(this.lastUpdate)
    return this.lastUpdate;
  }

  public async convertCurrency(payload: any): Promise<any> {
    const url = 'http://localhost:8080/api/v1/xchange/converter'
    this.http.post<any>(url, payload, {headers: {}}).subscribe({
      next: (response) => {
        return response.json();
      },
    });
  }

  public getCurrenciesPromise() {
    return new Promise<any>((resolve, reject) => {
      if (this.currencies.length == 0) {
        this.http.get<any>('http://localhost:8080/api/v1/xchange/getCurrentExchangeRates?type=lt').subscribe(response => {
            if (response.success) {
              this.currencies = [];
              response.data.FxRate.forEach(rate => {
                console.log(rate)
                rate.CcyAmt.forEach((ccyAmt: { Ccy: string, Amt: number }) => {
                  if (ccyAmt.Ccy !== "EUR") {
                    let currency: Currency = {
                      name: ccyAmt.Ccy,
                      rate: ccyAmt.Amt,
                      full_name: '',
                      symbol: ''
                    };
                    this.currencies.push(currency);
                    console.log(currency)
                  }
                });
              });
            }
            console.log(response);
            this.lastUpdate = response.timestamp;
            this.http.get<any>('https://restcountries.com/v3.1/all?fields=currencies').subscribe(data => {
                console.log(data);

                data.forEach(currency => {
                    let name = Object.keys(currency.currencies)[0]
                    var index = this.currencies.findIndex(element => element.name == name);
                    if (index != -1)
                      this.currencies[index] = {
                        ...this.currencies[index],
                        full_name: currency.currencies[name].name,
                        symbol: currency.currencies[name].symbol
                      }
                  }
                )
                resolve(this.currencies);
              },
              () => {
                reject();
              }
            )
          },
          () => {
            reject();
          }
        )
      } else {
        resolve(this.currencies);
      }
    })
  }
}
