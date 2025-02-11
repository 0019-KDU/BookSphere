import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BookRoutingModule } from './book-routing.module';
import { FormsModule } from '@angular/forms';
import { MenuComponent } from './components/menu/menu.component';
import { MainComponent } from './pages/main/main.component';
import { BookListComponent } from './pages/book-list/book-list.component';
import { RatingComponent } from './components/rating/rating.component';
import { BookCardComponent } from './components/book-card/book-card.component';
import { MyBooksComponent } from './pages/my-books/my-books.component';

@NgModule({
  declarations: [
    MainComponent,
    MenuComponent,
    BookListComponent,
    RatingComponent,
    BookCardComponent,
    MyBooksComponent,
  ],
  imports: [CommonModule, BookRoutingModule, FormsModule],
})
export class BookModule {}
