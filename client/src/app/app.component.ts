import {
  AfterViewInit,
  Component,
  OnInit,
  ViewChild,
} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {HttpClient} from '@angular/common/http';
import {Currency} from "./Currency";

import {CurrencyService} from "./currency-service/currency-service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, AfterViewInit {

  _from: any;
  to: Currency = {
    rate: 0,
    full_name: '',
    name: '',
    symbol: ''
  };
  amount_value: number = 12;

  title = 'currency-exchange';
  isDataAvailable = false;
  failedToLoad = false;
  currencies: Currency[] = [];


  @ViewChild('from') fromCmp;
  @ViewChild('to') toCmp;
  @ViewChild('amount_input', {static: false}) amount_input = 1;
  @ViewChild('submitBtn', {static: false}) submitBtn;
  @ViewChild('formExchange', {static: false}) formExchange;

  public resultFrom;
  public resultTo;
  public resultInfo;
  public isResult = false;
  public lastUpdate;

  public currency_data;

  get from_symbol() {
    return this._from.symbol;
  }


  constructor(private modalService: NgbModal, public service: CurrencyService, private http: HttpClient) {
  }

  public open(modal: any): void {
    this.modalService.open(modal);
  }

  public selectFrom = (currency: Currency): void => {
    this._from = currency;
    if (this.isResult)
      this.exchange();

  }

  public selectTo = (currency: Currency): void => {
    this.to = currency;
    if (this.isResult)
      this.exchange();
  }

  changeAmountValue($event: string) {
    // console.log($event);
    this.amount_value = Number((Math.round(Number($event) * 100) / 100).toFixed(2));
    localStorage.setItem("amount", String(this.amount_value));
    if (this.isResult)
      this.exchange();
  }


  public switchCurrencies() {
    let temp: Currency = this._from;
    this.fromCmp.selectCurrency(this.to);
    this.toCmp.selectCurrency(temp);
    if (this.isResult)
      this.exchange();
  }

  public async convertCurrency(payload: any): Promise<any> {
    const url = 'http://localhost:8080/api/v1/xchange/converter'
    this.http.post<any>(url, payload, {headers: {}}).subscribe({
      next: (response: { json: () => any; }) => {
        return response.json();
      },
    });
  }

  public exchange() {
    let rateBase = this.to.rate / this._from.rate;
    let result = this.amount_value * rateBase;

    this.resultFrom = this.amount_value + " " + (this._from.full_name ? this._from.full_name : this._from.name) + " =";
    this.resultTo = (result).toFixed(5) + " " + (this.to.full_name ? this.to.full_name : this.to.name);
    this.resultInfo = (1).toFixed(2) + " " + this._from.name + " = " + rateBase.toFixed(6) + " " + this.to.name + '\n '
      + (1).toFixed(2) + " " + this.to.name + " = " + (1 / rateBase).toFixed(6) + " " + this._from.name;
    this.isResult = true
    console.log(this.resultFrom)
  }

  onSubmit(): void {
    this.exchange();
    const date = new Date(this.service.getLastUpdate());
    this.lastUpdate = date.toLocaleString() + " UTC";
  }

  ngOnInit(): void {
    this.service.getCurrenciesPromise().then((data) => {
        console.log(data)
        this._from = data[0];
        this.to = data[1];
        this.isDataAvailable = true

      },
      () => {
        this.failedToLoad = true;
      }
    );

    let localAmount = localStorage.getItem("amount");
    this.amount_value = localAmount ? Number(localAmount) : Number((1).toFixed(2));
  }

  onResize(): void {
    this.formExchange.nativeElement.style.width = `${this.formExchange.nativeElement.clientWidth}px`;
  }

  ngAfterViewInit(): void {

  }
}
