import {
  Component, EventEmitter, Input, OnChanges,
  OnInit, Output, SimpleChanges,
} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Currency} from "../Currency";


@Component({
  selector: 'currency-result-display',
  templateUrl: './currency-display.component.html',
  styleUrls: ['./currency-display.component.html']
})
export class CurrencyDisplayComponent implements OnInit, OnChanges {
  currencies: Currency[] = [];

  @Input() resultFrom?: string;
  @Input() resultTo?: string;
  @Input() resultInfo?: string;
  @Output() resultChange = new EventEmitter<string>();

  localResult?: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['resultFrom'] && changes['resultFrom'].currentValue) ||
      (changes['resultTo'] && changes['resultTo'].currentValue) || (changes['resultInfo'] && changes['resultInfo'].currentValue)) {
      this.localResult = this.resultFrom;
    }
  }

  constructor(private modalService: NgbModal) {
  }


  ngOnInit(): void {
    console.log(this.localResult, this.resultFrom);
  }

}
