import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-currencies',
  templateUrl: './currencies.component.html',
  // styleUrls: ['./currencies.component.scss']
})
export class CurrenciesComponent {

  @Input() selectCurrency: any;
  @Input() currency: any;


  public selectCurrencyFunc(selectedCurrency: any): void {
    console.log(selectedCurrency);
    if (this.selectCurrency) {
      this.selectCurrency(selectedCurrency);
    }
  }
}
