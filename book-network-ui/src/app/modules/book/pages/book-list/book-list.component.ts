import { Component, OnInit } from '@angular/core';
import { BookService } from '../../../../services/services/book.service';
import { Router } from '@angular/router';
import { PageResponseBookResponse } from '../../../../services/models/page-response-book-response';
import { CommonModule } from '@angular/common';
import { BookCardComponent } from '../../components/book-card/book-card.component';

@Component({
  selector: 'app-book-list',
  standalone: true, // If using standalone components
  imports: [CommonModule, BookCardComponent],
  templateUrl: './book-list.component.html',
  styleUrls: ['./book-list.component.scss'], // ✅ Fixed typo
})
export class BookListComponent implements OnInit {
  bookResponse: PageResponseBookResponse = {} as PageResponseBookResponse;
  page = 0;
  size = 5;
  pages: any = [];
  message = '';
  level: 'success' | 'error' = 'success';

  constructor(private bookService: BookService, private route: Router) {}

  ngOnInit(): void {
    this.findAllBooks();
  }

  private findAllBooks() {
    this.bookService
      .findAllBooks({
        page: this.page,
        size: this.size,
      })
      .subscribe({
        next: (books) => {
          this.bookResponse = books;
        },
        error: (err) => {
          this.message = 'Failed to fetch books';
          this.level = 'error';
          console.error(err);
        },
      });
  }
}
