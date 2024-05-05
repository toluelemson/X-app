import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {AppComponent} from './app.component';
import {CurrenciesComponent} from './currencies/currencies.component';
import {FormsModule} from '@angular/forms';
import {CurrencySelectorComponent} from './currency-selector/currency-selector.component';
import {CurrencyService} from './currency-service/currency-service';
import {HttpClientModule} from '@angular/common/http';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatSelectModule} from "@angular/material/select";
import {GraphComponent} from "./graph/graph.component";
import {MatGridListModule} from "@angular/material/grid-list";
import {CurrencyDisplayComponent} from "./currency-display/currency-display.component";
import {CurrencyAmountComponent} from "./currency-amount-form/currency-amount.component";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatDividerModule} from "@angular/material/divider";
import {ButtonOverviewExample} from "./button/button.component";
import {MatChipList, MatChipsModule} from "@angular/material/chips";
import {MatButtonToggleModule} from "@angular/material/button-toggle";


@NgModule({
  declarations: [
    AppComponent,
    CurrenciesComponent,
    CurrencySelectorComponent,
    GraphComponent,
    CurrencyDisplayComponent,
    CurrencyAmountComponent
  ],
  imports: [
    MatFormFieldModule,

    MatInputModule,
    MatSelectModule,
    BrowserModule,
    FormsModule,
    NgbModule,
    HttpClientModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    BrowserAnimationsModule,
    MatGridListModule,
    MatExpansionModule, MatButtonModule, MatDividerModule, MatIconModule, MatButtonModule, MatDividerModule, MatIconModule, ButtonOverviewExample, MatChipsModule, MatButtonToggleModule],
  providers: [CurrencyService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
