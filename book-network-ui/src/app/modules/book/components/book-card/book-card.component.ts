import { Component, Input } from '@angular/core';
import { BookResponse } from '../../../../services/models';

@Component({
  selector: 'app-book-card',
  imports: [],
  templateUrl: './book-card.component.html',
  styleUrl: './book-card.component.scss',
})
export class BookCardComponent {
  private _book: BookResponse = {};
  private _bookCover: String | undefined;

  get book(): BookResponse {
    return this._book;
  }

  @Input()
  set book(value: BookResponse) {
    this._book = value;
  }

  get bookCover(): String | undefined {
    if (this._book.cover) {
      return;
    }
    return this._bookCover;
  }
}
