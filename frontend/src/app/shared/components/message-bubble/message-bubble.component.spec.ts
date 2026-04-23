import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageBubbleComponent } from './message-bubble.component';
import { ChatMessageViewModel } from '../../../core/models/api.models';

describe('MessageBubbleComponent', () => {
  let fixture: ComponentFixture<MessageBubbleComponent>;

  const assistantMessage: ChatMessageViewModel = {
    id: 'message-1',
    role: 'ASSISTANT',
    content: 'Here is the grounded answer.',
    createdAt: '2026-04-23T10:00:00Z',
    citations: [
      {
        chunkId: 'chunk-1',
        projectId: 'project-1',
        documentId: 'document-1',
        sourceFileName: 'guide.pdf',
        chunkIndex: 3,
        excerpt: 'Important cited text.'
      }
    ]
  };

  beforeEach(async () => {
    spyOn(Date.prototype, 'toLocaleTimeString').and.returnValue('10:00 AM');

    await TestBed.configureTestingModule({
      imports: [MessageBubbleComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(MessageBubbleComponent);
    fixture.componentRef.setInput('message', assistantMessage);
    fixture.detectChanges();
  });

  it('renders the assistant label, formatted time, and citations', () => {
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.message')?.classList.contains('message--assistant')).toBeTrue();
    expect(element.textContent).toContain('RAG Assistant');
    expect(element.textContent).toContain('10:00 AM');
    expect(element.textContent).toContain('guide.pdf');
    expect(element.textContent).toContain('chunk 3');
    expect(element.textContent).toContain('Important cited text.');
  });

  it('renders a user label for user messages', () => {
    fixture.componentRef.setInput('message', {
      ...assistantMessage,
      role: 'USER',
      citations: []
    });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('You');
  });
});
