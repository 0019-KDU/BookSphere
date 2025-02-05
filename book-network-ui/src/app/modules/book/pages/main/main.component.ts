import { Component } from '@angular/core';
import { MenuComponent } from '../../components/menu/menu.component';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
  imports: [MenuComponent, RouterModule],
})
export class MainComponent {}
