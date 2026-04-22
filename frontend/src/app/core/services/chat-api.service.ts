import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatRequest, ChatResponse, ConversationResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ChatApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/chat';

  sendMessage(payload: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.baseUrl}/messages`, payload);
  }

  getConversation(conversationId: string): Observable<ConversationResponse> {
    return this.http.get<ConversationResponse>(`${this.baseUrl}/conversations/${conversationId}`);
  }

  deleteConversation(conversationId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/conversations/${conversationId}`);
  }
}
