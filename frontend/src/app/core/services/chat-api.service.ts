import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatRequest, ChatResponse, ConversationResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ChatApiService {
  private readonly http = inject(HttpClient);

  sendMessage(projectId: string, payload: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.baseUrl(projectId)}/messages`, payload);
  }

  getConversation(projectId: string, conversationId: string): Observable<ConversationResponse> {
    return this.http.get<ConversationResponse>(`${this.baseUrl(projectId)}/conversations/${conversationId}`);
  }

  deleteConversation(projectId: string, conversationId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(projectId)}/conversations/${conversationId}`);
  }

  private baseUrl(projectId: string): string {
    return `/api/v1/projects/${projectId}/chat`;
  }
}
