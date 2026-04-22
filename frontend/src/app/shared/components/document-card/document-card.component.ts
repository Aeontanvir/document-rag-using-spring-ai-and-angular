import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DocumentMetadata } from '../../../core/models/api.models';

@Component({
  selector: 'app-document-card',
  templateUrl: './document-card.component.html',
  styleUrl: './document-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DocumentCardComponent {
  readonly document = input.required<DocumentMetadata>();
  readonly remove = output<string>();

  protected onRemove(): void {
    this.remove.emit(this.document().id);
  }

  protected formatSize(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} B`;
    }

    const kb = bytes / 1024;
    if (kb < 1024) {
      return `${kb.toFixed(1)} KB`;
    }

    return `${(kb / 1024).toFixed(1)} MB`;
  }
}
