import { Component, EventEmitter, Input, Output } from '@angular/core';
import { BookResponse } from '../../../../services/models';
import { CommonModule } from '@angular/common';
import { RatingComponent } from '../rating/rating.component';

@Component({
  selector: 'app-book-card',
  standalone: false,
  templateUrl: './book-card.component.html',
  styleUrl: './book-card.component.scss',
})
export class BookCardComponent {
  private _book: BookResponse = {};
  private _bookCover: String | undefined;
  private _manage = false;
  get manage(): boolean {
    return this._manage;
  }

  @Input()
  set manage(value: boolean) {
    this._manage = value;
  }
  get book(): BookResponse {
    return this._book;
  }

  @Input()
  set book(value: BookResponse) {
    this._book = value;
  }

  get bookCover(): String | undefined {
    if (this._book.cover) {
      return 'data:image/jpeg;base64,' + this._book.cover;
    }
    return 'https://source.unsplash.com/user/c_v_r/1900x800';
  }

  @Output() private share: EventEmitter<BookResponse> =
    new EventEmitter<BookResponse>();
  @Output() private archive: EventEmitter<BookResponse> =
    new EventEmitter<BookResponse>();
  @Output() private addToWaitingList: EventEmitter<BookResponse> =
    new EventEmitter<BookResponse>();
  @Output() private borrow: EventEmitter<BookResponse> =
    new EventEmitter<BookResponse>();
  @Output() private edit: EventEmitter<BookResponse> =
    new EventEmitter<BookResponse>();
  @Output() private details: EventEmitter<BookResponse> =
    new EventEmitter<BookResponse>();
  onShare() {
    this.share.emit(this._book);
  }

  onArchive() {
    this.archive.emit(this._book);
  }

  onAddToWaitingList() {
    this.addToWaitingList.emit(this._book);
  }

  onBorrow() {
    this.borrow.emit(this._book);
  }

  onEdit() {
    this.edit.emit(this._book);
  }

  onShowDetails() {
    this.details.emit(this._book);
  }
}
