import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ChatApiService } from './chat-api.service';

describe('ChatApiService', () => {
  let service: ChatApiService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ChatApiService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(ChatApiService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('sends chat messages to the project chat endpoint', () => {
    const payload = { prompt: 'What is in the docs?', topK: 5 };

    service.sendMessage('project-1', payload).subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects/project-1/chat/messages');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ conversationId: 'conversation-1' });
  });

  it('loads a saved conversation by id', () => {
    service.getConversation('project-1', 'conversation-1').subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects/project-1/chat/conversations/conversation-1');
    expect(request.request.method).toBe('GET');
    request.flush({ messages: [] });
  });

  it('deletes a saved conversation by id', () => {
    service.deleteConversation('project-1', 'conversation-1').subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects/project-1/chat/conversations/conversation-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
