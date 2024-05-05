import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ElementRef, ViewChild,
} from '@angular/core';

import {HttpClient} from '@angular/common/http';
import Chart, {ChartConfiguration} from 'chart.js/auto';

interface RateHistory {
  Dt: string;
  CcyAmt: Array<{ Amt: number }>;
}

@Component({
  selector: 'app-graph',
  templateUrl: './graph.component.html',
  styleUrls: ['../graph/graph.component.scss', '../../styles.scss'],
})
export class GraphComponent implements OnChanges, OnInit {

  @ViewChild('chartCanvas') chartCanvas: ElementRef | undefined;

  @Input() text_color: any;
  @Input() text_color_nonheading: any;
  @Input() text_color_minitext: any;
  @Input() input_background: any;
  @Input() selector_background: any;

  @Input() currency_data?: { rateHistory: RateHistory[] };
  @Input() rateOneCurrency: string = 'EUR';
  @Input() rateTwoCurrency: string = 'USD';
  @Input() isResult!: boolean;

  public periods = [
    {label: 'TODAY', value: '0D'},
    {label: '1D', value: '1D'},
    {label: '5D', value: '5D'},
    {label: '1M', value: '1M'},
    {label: '1Y', value: '1Y'},
    // {label: '5Y', value: '5Y'},
  ];

  selectedPeriod = this.periods[0].value;

  rateGraphLabels: string[] = [];
  rateGraphCcyAmtData: number[] = [];
  graphTextLabel: string = '';
  chart?: Chart;
  calculatedDate: Date = new Date(this.getCurrentDateFormatted());


  isChartDataAvailable: undefined | boolean = false;

  private base_url: string = 'http://localhost:8080';

  constructor(private http: HttpClient) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['currency_data'] && changes['currency_data'].currentValue) ||
      (changes['rateOneCurrency'] && changes['rateOneCurrency'].currentValue) || (changes['rateTwoCurrency'] && changes['rateTwoCurrency'].currentValue)) {
      this.isChartDataAvailable = this.currency_data && this.currency_data.rateHistory.length > 0;

      this.updateGraph();
    }
  }

  ngOnInit(): void {
    this.updateGraph();
  }

  private updateGraph(): void {
    this.processGraphData();
    this.createChart();
    const dateFrom = this.formatDate(this.calculatedDate);
    const dateTo = this.getCurrentDateFormatted()
    this.fetchData(dateFrom, dateTo, this.rateTwoCurrency, this.rateTwoCurrency)
    this.isChartDataAvailable = true
  }

  private processGraphData(): void {
    if (!this.currency_data) return;
    const {rateHistory} = this.currency_data;
    this.rateGraphLabels = rateHistory.map(rate => rate.Dt);
    this.rateGraphCcyAmtData = rateHistory.map(rate => rate.CcyAmt[1].Amt);
    this.graphTextLabel = `1 ${this.rateOneCurrency} to ${this.rateTwoCurrency}`;
  }

  filterRateHistory(period: { label: string; value: string }): void {

    this.selectedPeriod = period.value;
    const match = period.value.match(/(\d+)([DMY])/);
    if (!match) {
      console.error('Invalid input format:', period.value);
      return;
    }

    const value = Number(match[1]);
    const unit = match[2];
    const currentDate = new Date();


    switch (unit) {
      case 'D':
        this.calculatedDate = new Date(currentDate.getTime() - value * 24 * 60 * 60 * 1000);
        break;
      case 'M':
        this.calculatedDate = new Date(currentDate.getFullYear(), currentDate.getMonth() - value, currentDate.getDate());
        break;
      case 'Y':
        this.calculatedDate = new Date(currentDate.getFullYear() - value, currentDate.getMonth(), currentDate.getDate());
        break;
      default:
        this.calculatedDate = new Date(currentDate.getDate());
        return;
    }

    const dateTo = this.formatDate(this.calculatedDate);
    const dateFrom = this.formatDate(new Date(this.getCurrentDateFormatted()));
    this.fetchData(dateFrom, dateTo, this.rateOneCurrency, this.rateTwoCurrency);
    this.createChart();
  }

  private updateChartData(rateHistory: RateHistory[]): void {
    this.rateGraphLabels = rateHistory.map(rate => rate.Dt);
    this.rateGraphCcyAmtData = rateHistory.map(rate => rate.CcyAmt[1].Amt);
    this.createChart();
    this.isChartDataAvailable = true
  }

  fetchData(dateFrom: string, dateTo: string, toCurrency: string, fromCurrency: string): void {

    this.http
      .get(`${this.base_url}/api/v1/xchange/getFxRatesForCurrency?type=lt&dateFrom=${dateFrom}&dateTo=${dateTo}&baseCurrency=${toCurrency}&targetCurrency=${fromCurrency}`)
      .subscribe((response: any) => {
        // Check for the existence of data and that it contains the FxRate object
        if (!response || !response.data || !response.data.FxRate) {
          console.error('No data returned from fetch');
          return;
        }
        this.updateChartData(response.data.FxRate);

      }, error => {
        console.error('Failed to fetch data:', error);
      });
  }


  formatDate(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  getCurrentDateFormatted(): string {
    const currentDate = new Date();
    return this.formatDate(currentDate);
  }

  createChart() {
    if (!this.chartCanvas) {
      console.error('Canvas element is not available');
      return;
    }
    const context = this.chartCanvas.nativeElement.getContext('2d');
    if (!context) {
      console.error("Can't acquire context from the canvas element");
      return;
    }

    if (this.chart) {
      this.chart.destroy();
    }

    const chartConfig: ChartConfiguration = {
      type: 'line',
      data: {
        labels: this.rateGraphLabels,
        datasets: [{
          label: this.graphTextLabel,
          data: this.rateGraphCcyAmtData,
          backgroundColor: 'rgba(75, 192, 192, 1)',
          borderColor: 'black',
          borderWidth: 1,
          pointBackgroundColor: 'rgba(75, 192, 192, 1)',
          pointBorderColor: 'black',
          pointBorderWidth: 2,
          pointRadius: 5,
        }],
      },
      options: {
        plugins: {
          legend: {
            labels: {
              font: {
                size: 16,
                weight: 'bold',
              },
            },
          },
        },
        scales: {
          y: {
            ticks: {
              color: 'black',
              padding: 8,
            },
            grid: {
              color: 'black',
            },
          },
          x: {
            grid: {
              color: 'black',
            },
            ticks: {
              color: 'black',
            },
          },
        },
      }
    };

    this.chart = new Chart(context, chartConfig);
  }
}
