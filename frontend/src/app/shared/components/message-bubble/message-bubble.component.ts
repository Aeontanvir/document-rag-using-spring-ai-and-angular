import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ChatMessageViewModel } from '../../../core/models/api.models';

@Component({
  selector: 'app-message-bubble',
  templateUrl: './message-bubble.component.html',
  styleUrl: './message-bubble.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MessageBubbleComponent {
  readonly message = input.required<ChatMessageViewModel>();

  protected formatTime(timestamp: string): string {
    return new Date(timestamp).toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
