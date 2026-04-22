import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ChatMessageViewModel, DocumentMetadata } from '../../core/models/api.models';
import { ChatApiService } from '../../core/services/chat-api.service';
import { DocumentApiService } from '../../core/services/document-api.service';
import { DocumentCardComponent } from '../../shared/components/document-card/document-card.component';
import { MessageBubbleComponent } from '../../shared/components/message-bubble/message-bubble.component';

@Component({
  selector: 'app-chat-shell',
  imports: [CommonModule, FormsModule, MessageBubbleComponent, DocumentCardComponent],
  templateUrl: './chat-shell.component.html',
  styleUrl: './chat-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChatShellComponent {
  private readonly chatApi = inject(ChatApiService);
  private readonly documentApi = inject(DocumentApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly documents = signal<DocumentMetadata[]>([]);
  protected readonly messages = signal<ChatMessageViewModel[]>([]);
  protected readonly isUploading = signal(false);
  protected readonly isSending = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly uploadNotice = signal<string | null>(null);
  protected readonly conversationId = signal<string | null>(null);
  protected readonly documentCount = computed(() => this.documents().length);

  protected draft = '';

  constructor() {
    this.loadDocuments();
  }

  protected onFilesPicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    if (!files.length) {
      return;
    }

    this.isUploading.set(true);
    this.uploadNotice.set(null);
    this.errorMessage.set(null);

    this.documentApi
      .uploadDocuments(files)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.isUploading.set(false);
          input.value = '';
        })
      )
      .subscribe({
        next: (response) => {
          this.uploadNotice.set(`${response.uploadedCount} file(s) indexed into ChromaDB.`);
          this.loadDocuments();
        },
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Document upload failed.')
      });
  }

  protected sendPrompt(): void {
    const prompt = this.draft.trim();
    if (!prompt || this.isSending()) {
      return;
    }

    this.errorMessage.set(null);
    this.isSending.set(true);
    this.messages.update((messages) => [
      ...messages,
      this.toViewMessage('USER', prompt, new Date().toISOString())
    ]);
    this.draft = '';

    this.chatApi
      .sendMessage({
        conversationId: this.conversationId() ?? undefined,
        prompt,
        topK: 5
      })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSending.set(false))
      )
      .subscribe({
        next: (response) => {
          this.conversationId.set(response.conversationId);
          this.messages.update((messages) => [
            ...messages,
            {
              id: crypto.randomUUID(),
              role: 'ASSISTANT',
              content: response.answer,
              createdAt: response.respondedAt,
              citations: response.citations
            }
          ]);
        },
        error: (error) => {
          this.messages.update((messages) => [
            ...messages,
            this.toViewMessage(
              'ASSISTANT',
              error.error?.message ?? 'The backend could not complete the RAG request.',
              new Date().toISOString()
            )
          ]);
        }
      });
  }

  protected resetConversation(): void {
    this.messages.set([]);
    this.errorMessage.set(null);
    this.uploadNotice.set(null);
    this.draft = '';
    this.conversationId.set(null);
  }

  protected clearServerConversation(): void {
    const currentConversationId = this.conversationId();
    if (!currentConversationId) {
      this.resetConversation();
      return;
    }

    this.chatApi
      .deleteConversation(currentConversationId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.resetConversation(),
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to clear conversation.')
      });
  }

  protected removeDocument(documentId: string): void {
    this.documentApi
      .deleteDocument(documentId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadDocuments(),
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to delete document.')
      });
  }

  protected trackMessage(index: number, message: ChatMessageViewModel): string {
    return message.id;
  }

  protected trackDocument(index: number, document: DocumentMetadata): string {
    return document.id;
  }

  private loadDocuments(): void {
    this.documentApi
      .listDocuments()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (documents) => this.documents.set(documents),
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to load documents.')
      });
  }

  private toViewMessage(role: 'USER' | 'ASSISTANT', content: string, createdAt: string): ChatMessageViewModel {
    return {
      id: crypto.randomUUID(),
      role,
      content,
      createdAt,
      citations: []
    };
  }
}
