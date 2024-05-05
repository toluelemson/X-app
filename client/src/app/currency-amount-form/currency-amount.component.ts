import {
  Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild,
} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Currency} from "../Currency";


@Component({
  selector: 'currency-amount-display',
  templateUrl: './currency-amount.component.html',
  styleUrls: ['../../styles/default.scss', './currency-amount.component.scss']
})
export class CurrencyAmountComponent {
  currencies: Currency[] = [];

  @Input() from_symbol?: string = '$';
  @Input() amount_value?: string = "1";
  @Input() change_amount_value?: any;
  @Output() changeAmountValue = new EventEmitter<string>();

  @ViewChild('formExchange', {static: false}) formExchange!: {
    nativeElement: { style: { width: string; }; clientWidth: any; };
  };

  constructor(private modalService: NgbModal) {
  }

  onAmountChange(newValue: string | undefined) {
    this.amount_value = newValue;
    this.changeAmountValue.emit(this.amount_value);
    console.log('onAmountChange', newValue, this.amount_value);
  }
}
